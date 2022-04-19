package dev.kord.common.entity

import dev.kord.common.Locale
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalSnowflake
import dev.kord.common.entity.optional.orEmpty
import dev.kord.common.serialization.DurationInDaysSerializer
import dev.kord.common.serialization.DurationInSecondsSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlin.time.Duration
import dev.kord.common.Color as CommonColor
import dev.kord.common.entity.DefaultMessageNotificationLevel as CommonDefaultMessageNotificationLevel
import dev.kord.common.entity.ExplicitContentFilter as CommonExplicitContentFilter
import dev.kord.common.entity.MFALevel as CommonMFALevel
import dev.kord.common.entity.Permissions as CommonPermissions
import dev.kord.common.entity.VerificationLevel as CommonVerificationLevel

@Serializable
public data class DiscordAuditLog(
    @SerialName("audit_log_entries")
    val auditLogEntries: List<DiscordAuditLogEntry>,
    @SerialName("guild_scheduled_events")
    val guildScheduledEvents: List<DiscordGuildScheduledEvent>,
    val integrations: List<DiscordPartialIntegration>,
    val threads: List<DiscordChannel>,
    val users: List<DiscordUser>,
    val webhooks: List<DiscordWebhook>,
)

@Serializable
public data class DiscordAuditLogEntry(
    @SerialName("target_id")
    val targetId: Snowflake?,
    val changes: Optional<List<AuditLogChange<@Contextual Any?>>> = Optional.Missing(),
    @SerialName("user_id")
    val userId: Snowflake?,
    val id: Snowflake,
    @SerialName("action_type")
    val actionType: AuditLogEvent,
    val options: Optional<AuditLogEntryOptionalInfo> = Optional.Missing(),
    val reason: Optional<String> = Optional.Missing(),
) {

    @Suppress("UNCHECKED_CAST")
    public operator fun <T> get(value: AuditLogChangeKey<T>): AuditLogChange<T>? =
        changes.orEmpty().firstOrNull { it.key == value } as? AuditLogChange<T>

}

/*
Do not trust the docs:
2020-11-12 (still true on 2022-03-09) all fields are described as present but are in fact optional
 */
@Serializable
public data class AuditLogEntryOptionalInfo(
    @SerialName("channel_id")
    val channelId: OptionalSnowflake = OptionalSnowflake.Missing,
    val count: Optional<String> = Optional.Missing(),
    @SerialName("delete_member_days")
    val deleteMemberDays: Optional<String> = Optional.Missing(),
    val id: OptionalSnowflake = OptionalSnowflake.Missing,
    @SerialName("members_removed")
    val membersRemoved: Optional<String> = Optional.Missing(),
    @SerialName("message_id")
    val messageId: OptionalSnowflake = OptionalSnowflake.Missing,
    @SerialName("role_name")
    val roleName: Optional<String> = Optional.Missing(),
    val type: Optional<OverwriteType> = Optional.Missing(),
)

@Serializable(with = AuditLogChange.Serializer::class)
public data class AuditLogChange<T>(
    val new: T?,
    val old: T?,
    val key: AuditLogChangeKey<T>,
) {

    internal class Serializer<T>(type: KSerializer<T>) : KSerializer<AuditLogChange<T>> {
        private val keySerializer = AuditLogChangeKey.serializer(type)

        override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor("dev.kord.common.entity.AuditLogChange", type.descriptor) {
                element("new_value", type.descriptor, isOptional = true)
                element("old_value", type.descriptor, isOptional = true)
                element("key", keySerializer.descriptor)
            }

        override fun deserialize(decoder: Decoder): AuditLogChange<T> = decoder.decodeStructure(descriptor) {
            var new: JsonElement? = null
            var old: JsonElement? = null
            lateinit var key: AuditLogChangeKey<T>

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> new = decodeSerializableElement(descriptor, index, JsonElement.serializer())
                    1 -> old = decodeSerializableElement(descriptor, index, JsonElement.serializer())
                    2 -> key = decodeSerializableElement(descriptor, index, keySerializer)

                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("Unexpected index: $index")
                }
            }

            val newVal = new?.let { Json.decodeFromJsonElement(key.serializer, new) }
            val oldVal = old?.let { Json.decodeFromJsonElement(key.serializer, old) }

            AuditLogChange(new = newVal, old = oldVal, key)
        }

        override fun serialize(encoder: Encoder, value: AuditLogChange<T>) = encoder.encodeStructure(descriptor) {
            value.new?.let { encodeSerializableElement(descriptor, 0, value.key.serializer, it) }
            value.old?.let { encodeSerializableElement(descriptor, 1, value.key.serializer, it) }
            encodeSerializableElement(descriptor, 2, keySerializer, value.key)
        }
    }
}

@Serializable(with = AuditLogChangeKey.Serializer::class)
public sealed class AuditLogChangeKey<T>(public val name: String, public val serializer: KSerializer<T>) {

    override fun toString(): String = "AuditLogChangeKey(name=$name)"

    public class Unknown(name: String) : AuditLogChangeKey<JsonElement>(name, JsonElement.serializer())

    /** Afk channel changed. */
    public object AfkChannelId : AuditLogChangeKey<Snowflake>("afk_channel_id", serializer())

    /** Afk timeout duration changed. */
    public object AfkTimeout : AuditLogChangeKey<Duration>("afk_timeout", DurationInSecondsSerializer)

    /** A permission on a text or voice channel was allowed for a role. */
    public object Allow : AuditLogChangeKey<CommonPermissions>("allow", serializer())

    /** Application id of the added or removed webhook or bot. */
    public object ApplicationId : AuditLogChangeKey<Snowflake>("application_id", serializer())

    /** Thread is now archived/unarchived. */
    public object Archived : AuditLogChangeKey<Boolean>("archived", serializer())

    /** Auto archive duration changed. */
    public object AutoArchiveDuration : AuditLogChangeKey<ArchiveDuration>("auto_archive_duration", serializer())

    /** Availability of sticker changed. */
    public object Available : AuditLogChangeKey<Boolean>("available", serializer())

    /** User avatar changed. */
    public object AvatarHash : AuditLogChangeKey<String>("avatar_hash", serializer())

    /** Guild banner changed. */
    public object BannerHash : AuditLogChangeKey<String>("banner_hash", serializer())

    /** Voice channel bitrate changed. */
    public object Bitrate : AuditLogChangeKey<Int>("bitrate", serializer())

    /** Channel for invite code or guild scheduled event changed. */
    public object ChannelId : AuditLogChangeKey<Snowflake>("channel_id", serializer())

    /** Invite code changed. */
    public object Code : AuditLogChangeKey<String>("code", serializer())

    /** Role color changed. */
    public object Color : AuditLogChangeKey<CommonColor>("color", serializer())

    /** Member timeout state changed. */
    public object CommunicationDisabledUntil : AuditLogChangeKey<Instant>("communication_disabled_until", serializer())

    /**	User server deafened/undeafened. */
    public object Deaf : AuditLogChangeKey<Boolean>("deaf", serializer())

    /** Default auto archive duration for newly created threads changed. */
    public object DefaultAutoArchiveDuration :
        AuditLogChangeKey<ArchiveDuration>("default_auto_archive_duration", serializer())

    @Deprecated(
        "Renamed to 'DefaultMessageNotifications'.",
        ReplaceWith("AuditLogChangeKey.DefaultMessageNotifications", "dev.kord.common.entity.AuditLogChangeKey"),
        DeprecationLevel.ERROR,
    )
    public object DefaultMessageNotificationLevel :
        AuditLogChangeKey<CommonDefaultMessageNotificationLevel>("default_message_notifications", serializer())

    /** Default message notification level changed. */
    public object DefaultMessageNotifications :
        AuditLogChangeKey<CommonDefaultMessageNotificationLevel>("default_message_notifications", serializer())

    /** A permission on a text or voice channel was denied for a role. */
    public object Deny : AuditLogChangeKey<CommonPermissions>("deny", serializer())

    /** Description changed. */
    public object Description : AuditLogChangeKey<String>("description", serializer())

    /** Discovery splash changed. */
    public object DiscoverySplashHash : AuditLogChangeKey<String>("discovery_splash_hash", serializer())

    /** Integration emoticons enabled/disabled. */
    public object EnableEmoticons : AuditLogChangeKey<Boolean>("enable_emoticons", serializer())

    /**	Entity type of guild scheduled event was changed. */
    public object EntityType : AuditLogChangeKey<ScheduledEntityType>("entity_type", serializer())

    /** Integration expiring subscriber behavior changed. */
    public object ExpireBehavior : AuditLogChangeKey<IntegrationExpireBehavior>("expire_behavior", serializer())

    /** Integration expire grace period changed. */
    public object ExpireGracePeriod : AuditLogChangeKey<Duration>("expire_grace_period", DurationInDaysSerializer)

    /** Change in whose messages are scanned and deleted for explicit content in the server. */
    public object ExplicitContentFilter :
        AuditLogChangeKey<CommonExplicitContentFilter>("explicit_content_filter", serializer())

    /** Format type of sticker changed. */
    public object FormatType : AuditLogChangeKey<MessageStickerType>("format_type", serializer())

    /** Guild sticker is in changed. */
    public object GuildId : AuditLogChangeKey<Snowflake>("guild_id", serializer())

    /** Role is now displayed/no longer displayed separate from online users. */
    public object Hoist : AuditLogChangeKey<Boolean>("hoist", serializer())

    /** Icon changed. */
    public object IconHash : AuditLogChangeKey<String>("icon_hash", serializer())

    /** Guild scheduled event cover image changed. */
    public object ImageHash : AuditLogChangeKey<String>("image_hash", serializer())

    /** The id of the changed entity - sometimes used in conjunction with other keys. */
    public object Id : AuditLogChangeKey<Snowflake>("id", serializer())

    /** Private thread is now invitable/uninvitable. */
    public object Invitable : AuditLogChangeKey<Boolean>("invitable", serializer())

    /** Person who created invite code changed */
    public object InviterId : AuditLogChangeKey<Snowflake>("inviter_id", serializer())

    /** Change in location for guild scheduled event. */
    public object Location : AuditLogChangeKey<String>("location", serializer())

    /** Thread is now locked/unlocked. */
    public object Locked : AuditLogChangeKey<Boolean>("locked", serializer())

    @Deprecated(
        "Renamed to 'MaxAge'.",
        ReplaceWith("AuditLogChangeKey.MaxAge", "dev.kord.common.entity.AuditLogChangeKey"),
        DeprecationLevel.ERROR,
    )
    public object MaxAges : AuditLogChangeKey<Duration>("max_age", DurationInSecondsSerializer)

    /** How long invite code lasts changed. */
    public object MaxAge : AuditLogChangeKey<Duration>("max_age", DurationInSecondsSerializer)

    /** Change to max number of times invite code can be used. */
    public object MaxUses : AuditLogChangeKey<Int>("max_uses", serializer())

    /** Role is now mentionable/unmentionable. */
    public object Mentionable : AuditLogChangeKey<Boolean>("mentionable", serializer())

    /** Two-factor auth requirement changed. */
    public object MFALevel : AuditLogChangeKey<CommonMFALevel>("mfa_level", serializer())

    /** User server muted/unmuted. */
    public object Mute : AuditLogChangeKey<Boolean>("mute", serializer())

    /** Name changed. */
    public object Name : AuditLogChangeKey<String>("name", serializer())

    /** User nickname changed. */
    public object Nick : AuditLogChangeKey<String>("nick", serializer())

    /** Channel nsfw restriction changed. */
    public object Nsfw : AuditLogChangeKey<Boolean>("nsfw", serializer())

    /** Owner changed. */
    public object OwnerId : AuditLogChangeKey<Snowflake>("owner_id", serializer())

    /** Permissions on a channel changed. */
    public object PermissionOverwrites : AuditLogChangeKey<List<Overwrite>>("permission_overwrites", serializer())

    /** Permissions for a role changed. */
    public object Permissions : AuditLogChangeKey<CommonPermissions>("permissions", serializer())

    /** Text or voice channel position changed. */
    public object Position : AuditLogChangeKey<Int>("position", serializer())

    /** Preferred locale changed. */
    public object PreferredLocale : AuditLogChangeKey<Locale>("preferred_locale", serializer())

    /** Privacy level of the stage instance or guild scheduled event changed. */
    public object PrivacyLevel : AuditLogChangeKey<Int>("privacy_level", serializer())

    /** Change in number of days after which inactive and role-unassigned members are kicked. */
    public object PruneDeleteDays : AuditLogChangeKey<Int>("prune_delete_days", serializer())

    /** ID of the public updates channel changed. */
    public object PublicUpdatesChannelId : AuditLogChangeKey<Snowflake>("public_updates_channel_id", serializer())

    /**	Amount of seconds a user has to wait before sending another message changed. */
    public object RateLimitPerUser : AuditLogChangeKey<Duration>("rate_limit_per_user", DurationInSecondsSerializer)

    /** Region changed. */
    public object Region : AuditLogChangeKey<String>("region", serializer())

    /** ID of the rules channel changed. */
    public object RulesChannelId : AuditLogChangeKey<Snowflake>("rules_channel_id", serializer())

    /** Invite splash page artwork changed. */
    public object SplashHash : AuditLogChangeKey<String>("splash_hash", serializer())

    /** Status of guild scheduled event was changed. */
    public object Status : AuditLogChangeKey<GuildScheduledEventStatus>("status", serializer())

    /** ID of the system channel changed. */
    public object SystemChannelId : AuditLogChangeKey<Snowflake>("system_channel_id", serializer())

    /** Related emoji of sticker changed. */
    public object Tags : AuditLogChangeKey<String>("tags", serializer())

    /** Invite code is temporary/never expires. */
    public object Temporary : AuditLogChangeKey<Boolean>("temporary", serializer())

    /** Text channel topic or stage instance topic changed. */
    public object Topic : AuditLogChangeKey<String>("topic", serializer())

    /**
     * Type of entity created.
     *
     * The actual supertype is [AuditLogChangeKey<Int | String>][AuditLogChangeKey] but Kotlin does not support union
     * types yet. [Int]s are instead converted to a [String].
     */
    public object Type : AuditLogChangeKey<String>("type", IntOrStringSerializer) {
        // TODO use union type `String | Int` if Kotlin ever introduces them

        // Audit Log Change Key "type" has integer or string values, so we need some sort of union serializer
        // (see https://discord.com/developers/docs/resources/audit-log#audit-log-entry-object-audit-log-change-key)
        private object IntOrStringSerializer : KSerializer<String> {
            private val backingSerializer = JsonPrimitive.serializer()

            /*
             * Delegating serializers should not reuse descriptors:
             * https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serializers.md#delegating-serializers
             *
             * however `SerialDescriptor("...", backingSerializer.descriptor)` will throw since
             * `JsonPrimitive.serializer().kind` is `PrimitiveKind.STRING` (`SerialDescriptor()` does not allow
             * `PrimitiveKind`)
             * -> use `PrimitiveSerialDescriptor("...", PrimitiveKind.STRING)` instead
             */
            override val descriptor = PrimitiveSerialDescriptor(
                serialName = "dev.kord.common.entity.AuditLogChangeKey.Type.IntOrString",
                PrimitiveKind.STRING,
            )

            override fun serialize(encoder: Encoder, value: String) {
                val jsonPrimitive = value.toIntOrNull()?.let { JsonPrimitive(it) } ?: JsonPrimitive(value)
                encoder.encodeSerializableValue(backingSerializer, jsonPrimitive)
            }

            override fun deserialize(decoder: Decoder): String {
                val jsonPrimitive = decoder.decodeSerializableValue(backingSerializer)
                return if (jsonPrimitive.isString) jsonPrimitive.content else jsonPrimitive.int.toString()
            }
        }
    }

    /** Role unicode emoji changed. */
    public object UnicodeEmoji : AuditLogChangeKey<String>("unicode_emoji", serializer())

    /** New user limit in a voice channel. */
    public object UserLimit : AuditLogChangeKey<Int>("user_limit", serializer())

    /** Number of times invite code was used changed. */
    public object Uses : AuditLogChangeKey<Int>("uses", serializer())

    /** Guild invite vanity url changed. */
    public object VanityUrlCode : AuditLogChangeKey<String>("vanity_url_code", serializer())

    /** Required verification level changed. */
    public object VerificationLevel : AuditLogChangeKey<CommonVerificationLevel>("verification_level", serializer())

    /** Channel id of the server widget changed. */
    public object WidgetChannelId : AuditLogChangeKey<Snowflake>("widget_channel_id", serializer())

    /** Server widget enabled/disabled. */
    public object WidgetEnabled : AuditLogChangeKey<Boolean>("widget_enabled", serializer())

    /** New role added. */
    public object Add : AuditLogChangeKey<List<DiscordPartialRole>>("\$add", serializer())

    /** Role removed. */
    public object Remove : AuditLogChangeKey<List<DiscordPartialRole>>("\$remove", serializer())


    internal object Serializer : KSerializer<AuditLogChangeKey<*>> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("dev.kord.common.entity.AuditLogChangeKey", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: AuditLogChangeKey<*>) {
            encoder.encodeString(value.name)
        }

        override fun deserialize(decoder: Decoder): AuditLogChangeKey<*> = when (val name = decoder.decodeString()) {
            "afk_channel_id" -> AfkChannelId
            "afk_timeout" -> AfkTimeout
            "allow" -> Allow
            "application_id" -> ApplicationId
            "archived" -> Archived
            "auto_archive_duration" -> AutoArchiveDuration
            "available" -> Available
            "avatar_hash" -> AvatarHash
            "banner_hash" -> BannerHash
            "bitrate" -> Bitrate
            "channel_id" -> ChannelId
            "code" -> Code
            "color" -> Color
            "communication_disabled_until" -> CommunicationDisabledUntil
            "deaf" -> Deaf
            "default_auto_archive_duration" -> DefaultAutoArchiveDuration
            "default_message_notifications" -> DefaultMessageNotifications
            "deny" -> Deny
            "description" -> Description
            "discovery_splash_hash" -> DiscoverySplashHash
            "enable_emoticons" -> EnableEmoticons
            "entity_type" -> EntityType
            "expire_behavior" -> ExpireBehavior
            "expire_grace_period" -> ExpireGracePeriod
            "explicit_content_filter" -> ExplicitContentFilter
            "format_type" -> FormatType
            "guild_id" -> GuildId
            "hoist" -> Hoist
            "icon_hash" -> IconHash
            "image_hash" -> ImageHash
            "id" -> Id
            "invitable" -> Invitable
            "inviter_id" -> InviterId
            "location" -> Location
            "locked" -> Locked
            "max_age" -> MaxAge
            "max_uses" -> MaxUses
            "mentionable" -> Mentionable
            "mfa_level" -> MFALevel
            "mute" -> Mute
            "name" -> Name
            "nick" -> Nick
            "nsfw" -> Nsfw
            "owner_id" -> OwnerId
            "permission_overwrites" -> PermissionOverwrites
            "permissions" -> Permissions
            "position" -> Position
            "preferred_locale" -> PreferredLocale
            "privacy_level" -> PrivacyLevel
            "prune_delete_days" -> PruneDeleteDays
            "public_updates_channel_id" -> PublicUpdatesChannelId
            "rate_limit_per_user" -> RateLimitPerUser
            "region" -> Region
            "rules_channel_id" -> RulesChannelId
            "splash_hash" -> SplashHash
            "status" -> Status
            "system_channel_id" -> SystemChannelId
            "tags" -> Tags
            "temporary" -> Temporary
            "topic" -> Topic
            "type" -> Type
            "unicode_emoji" -> UnicodeEmoji
            "user_limit" -> UserLimit
            "uses" -> Uses
            "vanity_url_code" -> VanityUrlCode
            "verification_level" -> VerificationLevel
            "widget_channel_id" -> WidgetChannelId
            "widget_enabled" -> WidgetEnabled
            "\$add" -> Add
            "\$remove" -> Remove
            else -> Unknown(name)
        }
    }
}

@Serializable(with = AuditLogEvent.Serializer::class)
public sealed class AuditLogEvent(public val value: Int) {
    public class Unknown(value: Int) : AuditLogEvent(value)
    public object GuildUpdate : AuditLogEvent(1)
    public object ChannelCreate : AuditLogEvent(10)
    public object ChannelUpdate : AuditLogEvent(11)
    public object ChannelDelete : AuditLogEvent(12)
    public object ChannelOverwriteCreate : AuditLogEvent(13)
    public object ChannelOverwriteUpdate : AuditLogEvent(14)
    public object ChannelOverwriteDelete : AuditLogEvent(15)
    public object MemberKick : AuditLogEvent(20)
    public object MemberPrune : AuditLogEvent(21)
    public object MemberBanAdd : AuditLogEvent(22)
    public object MemberBanRemove : AuditLogEvent(23)
    public object MemberUpdate : AuditLogEvent(24)
    public object MemberRoleUpdate : AuditLogEvent(25)
    public object MemberMove : AuditLogEvent(26)
    public object MemberDisconnect : AuditLogEvent(27)
    public object BotAdd : AuditLogEvent(28)
    public object RoleCreate : AuditLogEvent(30)
    public object RoleUpdate : AuditLogEvent(31)
    public object RoleDelete : AuditLogEvent(32)
    public object InviteCreate : AuditLogEvent(40)
    public object InviteUpdate : AuditLogEvent(41)
    public object InviteDelete : AuditLogEvent(42)
    public object WebhookCreate : AuditLogEvent(50)
    public object WebhookUpdate : AuditLogEvent(51)
    public object WebhookDelete : AuditLogEvent(52)
    public object EmojiCreate : AuditLogEvent(60)
    public object EmojiUpdate : AuditLogEvent(61)
    public object EmojiDelete : AuditLogEvent(62)
    public object MessageDelete : AuditLogEvent(72)
    public object MessageBulkDelete : AuditLogEvent(73)
    public object MessagePin : AuditLogEvent(74)
    public object MessageUnpin : AuditLogEvent(75)
    public object IntegrationCreate : AuditLogEvent(80)
    public object IntegrationUpdate : AuditLogEvent(81)
    public object IntegrationDelete : AuditLogEvent(82)
    public object StageInstanceCreate : AuditLogEvent(83)
    public object StageInstanceUpdate : AuditLogEvent(84)
    public object StageInstanceDelete : AuditLogEvent(85)
    public object StickerCreate : AuditLogEvent(90)
    public object StickerUpdate : AuditLogEvent(91)
    public object StickerDelete : AuditLogEvent(92)
    public object GuildScheduledEventCreate : AuditLogEvent(100)
    public object GuildScheduledEventUpdate : AuditLogEvent(101)
    public object GuildScheduledEventDelete : AuditLogEvent(102)
    public object ThreadCreate : AuditLogEvent(110)
    public object ThreadUpdate : AuditLogEvent(111)
    public object ThreadDelete : AuditLogEvent(112)


    internal object Serializer : KSerializer<AuditLogEvent> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("dev.kord.common.entity.AuditLogEvent", PrimitiveKind.INT)

        override fun serialize(encoder: Encoder, value: AuditLogEvent) {
            encoder.encodeInt(value.value)
        }

        override fun deserialize(decoder: Decoder): AuditLogEvent = when (val value = decoder.decodeInt()) {
            1 -> GuildUpdate
            10 -> ChannelCreate
            11 -> ChannelUpdate
            12 -> ChannelDelete
            13 -> ChannelOverwriteCreate
            14 -> ChannelOverwriteUpdate
            15 -> ChannelOverwriteDelete
            20 -> MemberKick
            21 -> MemberPrune
            22 -> MemberBanAdd
            23 -> MemberBanRemove
            24 -> MemberUpdate
            25 -> MemberRoleUpdate
            26 -> MemberMove
            27 -> MemberDisconnect
            28 -> BotAdd
            30 -> RoleCreate
            31 -> RoleUpdate
            32 -> RoleDelete
            40 -> InviteCreate
            41 -> InviteUpdate
            42 -> InviteDelete
            50 -> WebhookCreate
            51 -> WebhookUpdate
            52 -> WebhookDelete
            60 -> EmojiCreate
            61 -> EmojiUpdate
            62 -> EmojiDelete
            72 -> MessageDelete
            73 -> MessageBulkDelete
            74 -> MessagePin
            75 -> MessageUnpin
            80 -> IntegrationCreate
            81 -> IntegrationUpdate
            82 -> IntegrationDelete
            83 -> StageInstanceCreate
            84 -> StageInstanceUpdate
            85 -> StageInstanceDelete
            90 -> StickerCreate
            91 -> StickerUpdate
            92 -> StickerDelete
            100 -> GuildScheduledEventCreate
            101 -> GuildScheduledEventUpdate
            102 -> GuildScheduledEventDelete
            110 -> ThreadCreate
            111 -> ThreadUpdate
            112 -> ThreadDelete
            else -> Unknown(value)
        }
    }
}
