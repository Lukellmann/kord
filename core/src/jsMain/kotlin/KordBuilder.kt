package dev.kord.core.builder.kord

import io.ktor.client.engine.*

public actual class KordBuilder actual constructor(token: String) : BaseKordBuilder(token)

// CIO is not available on JS
internal actual fun HttpClientEngineConfig.isCIOEngineConfigAndHasTooFewConnections(shards: Int) = false
