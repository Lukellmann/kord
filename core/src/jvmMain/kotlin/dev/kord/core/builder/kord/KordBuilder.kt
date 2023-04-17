package dev.kord.core.builder.kord

import dev.kord.core.Kord
import dev.kord.gateway.Gateway
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

public actual class KordBuilder actual constructor(token: String) : BaseKordBuilder(token) {
    /**
     * Enable adding a [Runtime.addShutdownHook] to log out of the [Gateway] when the process is killed.
     */
    public var enableShutdownHook: Boolean = true

    override suspend fun build(): Kord {
        val kord = buildBase()
        if (enableShutdownHook) {
            Runtime.getRuntime().addShutdownHook(thread(false) {
                runBlocking {
                    kord.gateway.detachAll()
                }
            })
        }

        return kord
    }
}

internal actual fun HttpClientEngineConfig.isCIOEngineConfigAndHasTooFewConnections(shards: Int): Boolean {
    val recommended = shards * 2
    return this is CIOEngineConfig &&
        (maxConnectionsCount < recommended || endpoint.maxConnectionsPerRoute < recommended)
}
