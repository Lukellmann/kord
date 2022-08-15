@file:Suppress("UNUSED_VARIABLE", "unused") // todo remove

package dev.kord.gateway

import dev.kord.common.KordConfiguration
import dev.kord.gateway.GatewayImpl.ConnectionMode.NewSession
import dev.kord.gateway.GatewayImpl.ConnectionMode.ResumeSession
import dev.kord.gateway.GatewayImpl.ConnectionResult.*
import dev.kord.gateway.GatewayImpl.State.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.loop
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.util.zip.InflaterOutputStream
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

public fun Gateway(data: DefaultGatewayData): Gateway = GatewayImpl(data)

private val logger = KotlinLogging.logger { }

private class GatewayImpl(
    private val data: DefaultGatewayData,
) : Gateway, CoroutineScope by CoroutineScope(SupervisorJob() + data.dispatcher) {
    private inline val coroutineScope: CoroutineScope get() = this

    init {
        // this makes sure that external cancellation of coroutineScope will result in Detached state
        coroutineScope.coroutineContext.job.invokeOnCompletion {
            @OptIn(DelicateCoroutinesApi::class)
            when (state.value) {
                Detaching, Detached -> {} // scope was cancelled by detach(), we are fine
                Starting, is Running, Stopping, Stopped -> GlobalScope.launch { detach() }
            }
        }
    }

    // see https://discord.com/developers/docs/topics/gateway#connecting-gateway-url-query-string-params
    private val initialUrl = URLBuilder(data.url)
        .apply { // always specify API version and encoding
            parameters.appendIfNameAbsent("v", KordConfiguration.GATEWAY_VERSION.toString())
            parameters["encoding"] = "json" // we only support json encoding
        }
        .build()
    private val compression = initialUrl.parameters.contains("compress", "zlib-stream")


    // todo emit custom close events
    override val events: SharedFlow<Event> = data.eventFlow.asSharedFlow()

    private val _ping = MutableStateFlow<Duration?>(value = null)
    override val ping: StateFlow<Duration?> = _ping.asStateFlow()


    private val stateMutex = Mutex()

    /** Can only be set while holding [stateMutex]. Reads without are fine but can see [transient states][Transient]. */
    private val state = atomic<State>(initial = Stopped)

    // todo update
    /**
     * State transitions:
     * ```
     *                 stop     +----------+     stop
     *           +------------- | Stopping | <------------+
     *           |              +----------+              |
     *           |                                        |
     *           V                                        |
     *      +---------+  start  +----------+  start  +---------+
     * ---> | Stopped | ------> | Starting | ------> | Running |
     *      +---------+         +----------+         +---------+
     *           |                                        |
     *           |   detach                      detach   |
     *           +--------------+     +-------------------+
     *                          |     |
     *                          V     V
     *                       +-----------+  detach  +----------+
     *                       | Detaching | -------> | Detached |
     *                       +-----------+          +----------+
     * ```
     * [Transient] states ([Starting], [Stopping], [Detaching]) exist so that [send] does not have to use [stateMutex]
     * to read [state].
     */
    private sealed interface State {

        /** Transient state that can't be observed when [state] is read under [stateMutex]. */
        sealed interface Transient : State

        /** Initial state and state after calling [stop]. */
        object Stopped : State

        /** [Transient] state between [Stopped] and [Running] (set by [start]). */
        object Starting : Transient

        /** State during [start]. [start] suspends until [connection] is closed and no new connection is established. */
        class Running(val connection: Connection) : State

        /** [Transient] state between [Running] and [Stopped] (set by [stop]). */
        object Stopping : Transient

        /**
         * [Transient] state between [Running] or [Stopped] and [Detached] (set by [detach]).
         *
         * [coroutineScope][GatewayImpl.coroutineScope] will be cancelled at some point in this state.
         */
        object Detaching : Transient

        /**
         * Final state after calling [detach].
         *
         * [coroutineScope][GatewayImpl.coroutineScope] is always cancelled in this state.
         */
        object Detached : State
    }


    // todo supervisor job?
    //  also handle cancellation of start() call
    // todo check suspension points for cancellation in all functions
    // todo handle external cancellation of coroutineScope
    override suspend fun start(configuration: GatewayConfiguration): Unit = supervisorScope {
        val connection = stateMutex.withLock {
            state.value = when (val currentState = state.value) {
                Stopped -> Starting // Stopped -> Starting
                Detached -> error(DETACHED)
                is Running -> error(RUNNING)
                is Transient -> error(TRANSIENT_STATE + currentState)
            }
            val socket = data.client.webSocketSession { url(initialUrl) }
            val connection = Connection(socket, NewSession, configuration)
            state.value = Running(connection) // Starting -> Running
            connection
        }
        val result = connection.startAndJoin() // todo has to wait for connection end
    }

    override suspend fun send(command: Command) {
        // might be cancelled while sending or yielding, this is ok

        // lock-free, it only reads state, this allows early exit in transient states
        state.loop { currentState ->
            when (currentState) {
                is Running -> {
                    currentState.connection.send(command)
                    return // exit loop
                }
                // todo should we use yield()? -> yes if we suspend in Starting state
                Starting -> yield() // yield and keep looping until state changes to Running

                // early exit in transient states Stopping and Detaching
                Stopping, Stopped -> error(STOPPED)
                Detaching, Detached -> error(DETACHED)
            }
        }
    }

    override suspend fun stop() {
        // locking might suspend and is allowed to be cancelled, if cancelled, we don't execute the block inside
        stateMutex.withLock {
            val oldState = state.value
            state.value = when (oldState) {
                is Running -> Stopping // Running -> Stopping
                Detached, Stopped -> return // exit stop(), already stopped / detached
                is Transient -> error(TRANSIENT_STATE + oldState)
            }
            try {
                // Closing the connection is the only suspension point after successful lock. Because we are already in
                // Stopping state, we should not be cancelled before we are in Stopped state.
                // -> use NonCancellable
                withContext(NonCancellable) { oldState.connection.closeAndJoin() } // smart cast to Running
            } finally {
                state.value = Stopped // Stopping -> Stopped
            }
        }
    }

    override suspend fun detach() {
        // locking might suspend and is allowed to be cancelled, if cancelled, we don't execute the block inside
        stateMutex.withLock {
            val oldState = state.value
            state.value = when (oldState) {
                is Running, Stopped -> Detaching // Running -> Detaching or Stopped -> Detaching
                Detached -> return // exit detach(), already detached
                is Transient -> error(TRANSIENT_STATE + oldState)
            }
            try {
                // oldState can be Stopped or Running, otherwise already returned or thrown exception
                // end connection in case of Running, there is nothing to do in case of Stopped
                if (oldState is Running) {
                    // Closing the connection is the only suspension point after successful lock. Because we are already
                    // in Detaching state, we should not be cancelled before we are in Detached state.
                    // -> use NonCancellable
                    withContext(NonCancellable) { oldState.connection.closeAndJoin() }
                }
            } finally {
                // cancel before publishing Detached to make sure that scope is cancelled before being in final state
                coroutineScope.cancel()
                state.value = Detached // Detaching -> Detached
            }
        }
    }


    private data class ConnectionContext(
        val receivedHello: Boolean = false,
        val readyData: ReadyData? = null,
        val invalidSession: InvalidSession? = null,
        val reconnect: Boolean = false,
    ) {
        data class ReadyData(val sessionId: String, val resumeUrl: Url)
    }

    private sealed interface ConnectionMode {
        object NewSession : ConnectionMode
        class ResumeSession(val sessionId: String, val sequence: Int?) : ConnectionMode
    }

    private sealed interface ConnectionResult {
        object ReIdentifyAfterInvalidSession : ConnectionResult
        class ResumeAfterInvalidSession(val sessionId: String, val resumeUrl: Url, val sequence: Int?) :
            ConnectionResult

        object ReIdentifyAfterReconnectRequest : ConnectionResult
        class ResumeAfterReconnectRequest(val sessionId: String, val resumeUrl: Url, val sequence: Int?) :
            ConnectionResult

        fun toConnectionMode() = when (this) {
            ReIdentifyAfterInvalidSession, ReIdentifyAfterReconnectRequest -> NewSession
            is ResumeAfterInvalidSession -> ResumeSession(sessionId, sequence)
            is ResumeAfterReconnectRequest -> ResumeSession(sessionId, sequence)
        }
    }

    private interface FrameInflater : Closeable {
        fun inflate(frame: Frame): ByteArray
    }

    private object NoCompressionInflater : FrameInflater {
        override fun inflate(frame: Frame) = frame.data
        override fun close() {}
    }

    private class ZlibInflater : FrameInflater {
        private val byteOutput = ByteArrayOutputStream()
        private val inflaterOutput = InflaterOutputStream(byteOutput)

        override fun inflate(frame: Frame): ByteArray {
            inflaterOutput.write(frame.data)
            inflaterOutput.flush()
            val result = byteOutput.toByteArray()
            byteOutput.reset()
            return result
        }

        override fun close() {
            inflaterOutput.close()
        }
    }

    private inner class Connection(
        private val socket: DefaultClientWebSocketSession,
        private val mode: ConnectionMode,
        private val configuration: GatewayConfiguration,
    ) {

        // every connection to the gateway should use its own unique zlib context, see
        // https://discord.com/developers/docs/topics/gateway#transport-compression
        private val inflater = if (compression) ZlibInflater() else NoCompressionInflater

        // atomic: it's set in sequential processIncomingFrames() but read by concurrent heart-beating job
        private val sequence = atomic<Int?>(initial = null)

        private val possibleZombie = atomic(false)
        private val heartbeatTimeMark = atomic(TimeSource.Monotonic.markNow())


        suspend fun startAndJoin(): ConnectionResult = inflater.use { // close inflater, no matter what
            try {
                processIncomingFrames()
            } finally {
                // todo use this
                val closeReason = withTimeoutOrNull(1.5.seconds) { socket.closeReason.await() }
            }
        }

        private suspend fun processIncomingFrames(): ConnectionResult = coroutineScope scope@{
            // it's ok to be a var, channel reading loop is only accessing it sequentially
            var context = ConnectionContext()

            // don't buffer incoming frames for correct backpressure
            for (frame in socket.incoming) when (frame) {
                is Frame.Binary, is Frame.Text -> {

                    val eventJson = String(inflater.inflate(frame), Charsets.UTF_8)

                    val event = try {
                        logger.trace { "Gateway <<< $eventJson" }
                        GatewayJson.decodeFromString(Event.DeserializationStrategy, eventJson)
                    } catch (e: Throwable) { // don't let exception during deserialization kill Gateway
                        logger.catching(e)
                        null
                    }

                    if (event != null) {
                        // set sequence first, it is read by heart-beating job
                        if (event is DispatchEvent) event.sequence?.let { sequence.value = it }

                        context = handleEventAndGetUpdatedContext(event, context)

                        val result = context.getResult()

                        // publish event to outside world after we did our internal handling
                        data.eventFlow.emit(event)

                        if (result != null) {
                            this@scope.cancel() // todo should we do it this way?
                            // todo close socket etc
                            return@scope result
                        }
                    }
                }
                else -> {} // ignore other frame types, they are handled by DefaultWebSocketSession
            }

            TODO("what to return here?")
        }


        private fun ConnectionContext.getResult(): ConnectionResult? = when {
            reconnect -> {
                if (readyData == null) {
                    ReIdentifyAfterReconnectRequest
                } else {
                    ResumeAfterReconnectRequest(readyData.sessionId, readyData.resumeUrl, sequence.value)
                }
            }
            invalidSession != null -> {
                if (invalidSession.resumable) {
                    if (readyData == null) {
                        logger.warn("Received resumable Invalid Session before Ready event")
                        ReIdentifyAfterInvalidSession
                    } else {
                        ResumeAfterInvalidSession(readyData.sessionId, readyData.resumeUrl, sequence.value)
                    }
                } else ReIdentifyAfterInvalidSession
            }
            else -> null
        }


        private fun CoroutineScope.handleHello(heartbeatInterval: Int) {
            data.reconnectRetry.reset() // connected and read without problems, resetting retry counter

            launch { heartBeating(heartbeatInterval.toLong()) }

            val resumeOrIdentify = when (mode) {
                NewSession -> configuration.identify
                is ResumeSession -> Resume(configuration.token, mode.sessionId, mode.sequence ?: 0)
            }
            launch {
                data.identifyRateLimiter.consume(shardId = configuration.shard.index, events)
                send(resumeOrIdentify)
            }
        }

        private fun handleHeartbeatACK() {
            _ping.value = heartbeatTimeMark.value.elapsedNow()
            possibleZombie.value = false
        }

        private suspend fun heartBeating(intervalMillis: Long) {
            while (true) {
                if (possibleZombie.value) {
                    // todo what happens with this and how to handle?
                    socket.close(CLOSE_REASON_RECONNECTING)
                    break
                } else {
                    possibleZombie.value = true // reset on HeartbeatACK
                    sendHeartbeat()
                    delay(intervalMillis)
                }
            }
        }

        // non suspending, this is called sequentially for every frame, so it should be quick, or we can't process next
        // frame
        private fun CoroutineScope.handleEventAndGetUpdatedContext(
            event: Event,
            context: ConnectionContext,
        ): ConnectionContext = when (event) {
            // will be received most frequently -> put it first
            HeartbeatACK -> {
                handleHeartbeatACK()
                context
            }

            // heartbeat request, send it immediately without waiting the remainder of current interval
            is Heartbeat -> {
                launch { sendHeartbeat() } // this doesn't set possibleZombie
                context
            }

            // should only be received once
            is Hello -> {
                if (context.receivedHello) {
                    logger.warn("Received more than one Hello opcode")
                    context
                } else {
                    handleHello(event.heartbeatInterval)
                    context.copy(receivedHello = true)
                }
            }

            // should only be received once
            is Ready -> {
                if (context.readyData != null) {
                    logger.warn("Received more than one Ready event")
                    context
                } else {
                    val ready = event.data
                    val resumeUrl = URLBuilder(ready.resumeGatewayUrl)
                        .apply { parameters.appendMissing(initialUrl.parameters) } // keep custom query params
                        .build()
                    context.copy(readyData = ConnectionContext.ReadyData(ready.sessionId, resumeUrl))
                }
            }

            Reconnect -> context.copy(reconnect = true)

            is InvalidSession -> context.copy(invalidSession = event)

            // we don't handle other DispatchEvents and Close events are only emitted by ourselves
            is DispatchEvent, is Close -> context
        }

        suspend fun send(command: Command) {
            data.sendRateLimiter.consume()

            val commandJson = GatewayJson.encodeToString(Command.SerializationStrategy, command)

            logger.trace {
                val credentialFreeCopy = when (command) {
                    is Identify -> command.copy(token = "token")
                    is Resume -> command.copy(token = "token")
                    else -> null
                }
                val credentialFreeJson = credentialFreeCopy
                    ?.let { GatewayJson.encodeToString(Command.SerializationStrategy, it) }
                    ?: commandJson

                "Gateway >>> $credentialFreeJson"
            }

            socket.send(commandJson)
        }

        /** Like [send] but specialized for [Command.Heartbeat]: it sets [heartbeatTimeMark] before sending. */
        private suspend fun sendHeartbeat() {
            data.sendRateLimiter.consume()

            val json = GatewayJson.encodeToString(Command.SerializationStrategy, Command.Heartbeat(sequence.value))

            logger.trace { "Gateway >>> $json" }

            // for accurate ping: set heartbeat TimeMark right before send (most importantly after consume)
            heartbeatTimeMark.value = TimeSource.Monotonic.markNow()

            socket.send(json)
        }

        // called by stop() and detach()
        suspend fun closeAndJoin() {
            socket.close(CLOSE_REASON_LEAVING)
            awaitCancellation() // todo
        }
    }


    private companion object {
        private const val TRANSIENT_STATE = "Observed transient state while holding stateMutex: "
        private const val STOPPED = "The Gateway is not running, call start() first."
        private const val RUNNING = "The Gateway is already running, call stop() first."
        private const val DETACHED =
            "The Gateway has been detached and can no longer be used, create a new instance instead."

        private val CLOSE_REASON_LEAVING = CloseReason(CloseReason.Codes.NORMAL, message = "leaving")
        private val CLOSE_REASON_RECONNECTING = CloseReason(code = 4900, message = "reconnecting")

        private val GatewayJson = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}
