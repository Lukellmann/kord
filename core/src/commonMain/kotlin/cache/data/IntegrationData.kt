package dev.kord.core.cache.data

import dev.kord.common.entity.*
import dev.kord.common.entity.optional.*
import dev.kord.common.serialization.DurationInDays
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
public data class IntegrationData(
    val id: Snowflake,
    val guildId: Snowflake,
    val name: String,
    val type: IntegrationType,
    val enabled: Boolean,
    val syncing: OptionalBoolean = OptionalBoolean.Missing,
    val roleId: OptionalSnowflake = OptionalSnowflake.Missing,
    val enableEmoticons: OptionalBoolean = OptionalBoolean.Missing,
    val expireBehavior: Optional<IntegrationExpireBehavior> = Optional.Missing(),
    val expireGracePeriod: Optional<DurationInDays> = Optional.Missing(),
    val userId: OptionalSnowflake = OptionalSnowflake.Missing,
    val account: IntegrationsAccountData,
    val syncedAt: Optional<Instant> = Optional.Missing(),
    val subscriberCount: OptionalInt = OptionalInt.Missing,
    val revoked: OptionalBoolean = OptionalBoolean.Missing,
    val application: Optional<IntegrationApplication> = Optional.Missing(),
    val scopes: Optional<List<OAuth2Scope>> = Optional.Missing(),
) {
    @Deprecated("Binary compatibility, keep for some releases.", level = DeprecationLevel.HIDDEN)
    public fun getType(): String = type.value

    public companion object {

        public fun from(guildId: Snowflake, response: DiscordIntegration): IntegrationData = with(response) {
            IntegrationData(
                id,
                guildId,
                name,
                type,
                enabled,
                syncing,
                roleId,
                enableEmoticons,
                expireBehavior,
                expireGracePeriod,
                userId = user.mapSnowflake { it.id },
                account = IntegrationsAccountData.from(account),
                syncedAt,
                subscriberCount,
                revoked,
                application,
                scopes,
            )
        }
    }
}
