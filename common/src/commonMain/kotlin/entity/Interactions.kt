@file:Generate(
    INT_KORD_ENUM, name = "ApplicationCommandType",
    docUrl = "https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-types",
    entries = [
        Entry("ChatInput", intValue = 1, kDoc = "A text-based command that shows up when a user types `/`."),
        Entry("User", intValue = 2, kDoc = "A UI-based command that shows up when you right-click or tap on a user."),
        Entry(
            "Message", intValue = 3,
            kDoc = "A UI-based command that shows up when you right-click or tap on a message.",
        ),
    ],
)

@file:Generate(
    INT_KORD_ENUM, name = "ApplicationCommandOptionType", valueName = "type",
    docUrl = "https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-option-type",
    entries = [
        Entry("SubCommand", intValue = 1),
        Entry("SubCommandGroup", intValue = 2),
        Entry("String", intValue = 3),
        Entry("Integer", intValue = 4, kDoc = "Any integer between `-2^53` and `2^53`."),
        Entry("Boolean", intValue = 5),
        Entry("User", intValue = 6),
        Entry("Channel", intValue = 7, kDoc = "Includes all channel types + categories."),
        Entry("Role", intValue = 8),
        Entry("Mentionable", intValue = 9, kDoc = "Includes users and roles."),
        Entry("Number", intValue = 10, kDoc = "Any double between `-2^53` and `2^53`."),
        Entry("Attachment", intValue = 11),
    ],
)

@file:Generate(
    INT_KORD_ENUM, name = "InteractionType", valueName = "type",
    docUrl = "https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-interaction-type",
    entries = [
        Entry("Ping", intValue = 1),
        Entry("ApplicationCommand", intValue = 2),
        Entry("Component", intValue = 3),
        Entry("AutoComplete", intValue = 4),
        Entry("ModalSubmit", intValue = 5),
    ],
)

@file:Generate(
    INT_KORD_ENUM, name = "InteractionResponseType", valueName = "type",
    docUrl = "https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-response-object-interaction-callback-type",
    entries = [
        Entry("Pong", intValue = 1, kDoc = "ACK a [Ping][dev.kord.common.entity.InteractionType.Ping]."),
        Entry("ChannelMessageWithSource", intValue = 4, kDoc = "Respond to an interaction with a message."),
        Entry(
            "DeferredChannelMessageWithSource", intValue = 5,
            kDoc = "ACK an interaction and edit a response later, the user sees a loading state.",
        ),
        Entry(
            "DeferredUpdateMessage", intValue = 6,
            kDoc = "For components, ACK an interaction and edit the original message later; the user does not see a " +
                    "loading state.",
        ),
        Entry("UpdateMessage", intValue = 7, kDoc = "For components, edit the message the component was attached to."),
        Entry(
            "ApplicationCommandAutoCompleteResult", intValue = 8,
            kDoc = "Respond to an autocomplete interaction with suggested choices.",
        ),
        Entry("Modal", intValue = 9, kDoc = "Respond to an interaction with a popup modal."),
    ],
)

@file:Generate(
    INT_KORD_ENUM, name = "ApplicationCommandPermissionType",
    docUrl = "https://discord.com/developers/docs/interactions/application-commands#application-command-permissions-object-application-command-permission-type",
    entries = [
        Entry("Role", intValue = 1),
        Entry("User", intValue = 2),
        Entry("Channel", intValue = 3),
    ],
)

package dev.kord.common.entity

import dev.kord.common.Locale
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.entity.optional.*
import dev.kord.ksp.Generate
import dev.kord.ksp.Generate.EntityType.INT_KORD_ENUM
import dev.kord.ksp.Generate.Entry
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

@Serializable
public data class DiscordApplicationCommand(
    val id: Snowflake,
    val type: Optional<ApplicationCommandType> = Optional.Missing(),
    @SerialName("application_id")
    val applicationId: Snowflake,
    val name: String,
    @SerialName("name_localizations")
    val nameLocalizations: Optional<Map<Locale, String>?> = Optional.Missing(),
    /**
     * Don't trust the docs: This is nullable on non chat input commands.
     */
    val description: String?,
    @SerialName("description_localizations")
    val descriptionLocalizations: Optional<Map<Locale, String>?> = Optional.Missing(),
    @SerialName("guild_id")
    val guildId: OptionalSnowflake = OptionalSnowflake.Missing,
    val options: Optional<List<ApplicationCommandOption>> = Optional.Missing(),
    @SerialName("default_member_permissions")
    val defaultMemberPermissions: Permissions?,
    @SerialName("dm_permission")
    val dmPermission: OptionalBoolean = OptionalBoolean.Missing,
    @SerialName("default_permission")
    @Deprecated("'defaultPermission' is deprecated in favor of 'defaultMemberPermissions' and 'dmPermission'.")
    val defaultPermission: OptionalBoolean? = OptionalBoolean.Missing,
    val nsfw: OptionalBoolean = OptionalBoolean.Missing,
    val version: Snowflake
)

@Serializable
public data class ApplicationCommandOption(
    val type: ApplicationCommandOptionType,
    val name: String,
    @SerialName("name_localizations")
    val nameLocalizations: Optional<Map<Locale, String>?> = Optional.Missing(),
    val description: String,
    @SerialName("description_localizations")
    val descriptionLocalizations: Optional<Map<Locale, String>?> = Optional.Missing(),
    val default: OptionalBoolean = OptionalBoolean.Missing,
    val required: OptionalBoolean = OptionalBoolean.Missing,
    val choices: Optional<List<Choice<@Serializable(NotSerializable::class) Any?>>> = Optional.Missing(),
    val autocomplete: OptionalBoolean = OptionalBoolean.Missing,
    val options: Optional<List<ApplicationCommandOption>> = Optional.Missing(),
    @SerialName("channel_types")
    val channelTypes: Optional<List<ChannelType>> = Optional.Missing(),
    @SerialName("min_value")
    val minValue: Optional<JsonPrimitive> = Optional.Missing(),
    @SerialName("max_value")
    val maxValue: Optional<JsonPrimitive> = Optional.Missing(),
    @SerialName("min_length")
    val minLength: OptionalInt = OptionalInt.Missing,
    @SerialName("max_length")
    val maxLength: OptionalInt = OptionalInt.Missing
)

/**
 * A serializer whose sole purpose is to provide a No-Op serializer for [Any].
 * The serializer is used when the generic type is neither known nor relevant to the serialization process
 *
 * e.g: `Choice<@Serializable(NotSerializable::class) Any?>`
 * The serialization is handled by [Choice] serializer instead where we don't care about the generic type.
 */
@KordExperimental
public object NotSerializable : KSerializer<Any?> {
    override fun deserialize(decoder: Decoder): Nothing = error("This operation is not supported.")
    override val descriptor: SerialDescriptor = String.serializer().descriptor
    override fun serialize(encoder: Encoder, value: Any?): Nothing = error("This operation is not supported.")
}


private val LocalizationSerializer =
    Optional.serializer(MapSerializer(Locale.serializer(), String.serializer()).nullable)

@Serializable(Choice.Serializer::class)
public sealed class Choice<out T> {
    public abstract val name: String
    public abstract val nameLocalizations: Optional<Map<Locale, String>?>
    public abstract val value: T

    public data class IntegerChoice(
        override val name: String,
        override val nameLocalizations: Optional<Map<Locale, String>?>,
        override val value: Long,
    ) : Choice<Long>()

    public data class NumberChoice(
        override val name: String,
        override val nameLocalizations: Optional<Map<Locale, String>?>,
        override val value: Double
    ) : Choice<Double>()

    public data class StringChoice(
        override val name: String,
        override val nameLocalizations: Optional<Map<Locale, String>?>,
        override val value: String
    ) : Choice<String>()

    internal object Serializer : KSerializer<Choice<*>> {

        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Choice") {
            element<String>("name")
            element<JsonPrimitive>("value")
            element<Map<Locale, String>?>("name_localizations", isOptional = true)
        }

        override fun deserialize(decoder: Decoder) = decoder.decodeStructure(descriptor) {

            lateinit var name: String
            var nameLocalizations: Optional<Map<Locale, String>?> = Optional.Missing()
            lateinit var value: JsonPrimitive

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> name = decodeStringElement(descriptor, index)
                    1 -> value = decodeSerializableElement(descriptor, index, JsonPrimitive.serializer())
                    2 -> nameLocalizations = decodeSerializableElement(descriptor, index, LocalizationSerializer)

                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("unknown index: $index")
                }
            }

            when {
                value.isString -> StringChoice(name, nameLocalizations, value.content)
                else -> value.longOrNull?.let { IntegerChoice(name, nameLocalizations, it) }
                    ?: value.doubleOrNull?.let { NumberChoice(name, nameLocalizations, it) }
                    ?: throw SerializationException("Illegal choice value: $value")
            }
        }

        override fun serialize(encoder: Encoder, value: Choice<*>) = encoder.encodeStructure(descriptor) {

            encodeStringElement(descriptor, 0, value.name)

            when (value) {
                is IntegerChoice -> encodeLongElement(descriptor, 1, value.value)
                is NumberChoice -> encodeDoubleElement(descriptor, 1, value.value)
                is StringChoice -> encodeStringElement(descriptor, 1, value.value)
            }

            if (value.nameLocalizations !is Optional.Missing) {
                encodeSerializableElement(descriptor, 2, LocalizationSerializer, value.nameLocalizations)
            }
        }
    }
}

@Serializable
public data class ResolvedObjects(
    val members: Optional<Map<Snowflake, DiscordInteractionGuildMember>> = Optional.Missing(),
    val users: Optional<Map<Snowflake, DiscordUser>> = Optional.Missing(),
    val roles: Optional<Map<Snowflake, DiscordRole>> = Optional.Missing(),
    val channels: Optional<Map<Snowflake, DiscordChannel>> = Optional.Missing(),
    val messages: Optional<Map<Snowflake, DiscordMessage>> = Optional.Missing(),
    val attachments: Optional<Map<Snowflake, DiscordAttachment>> = Optional.Missing()
)

@Serializable
public data class DiscordInteraction(
    val id: Snowflake,
    @SerialName("application_id")
    val applicationId: Snowflake,
    val type: InteractionType,
    val data: InteractionCallbackData,
    @SerialName("guild_id")
    val guildId: OptionalSnowflake = OptionalSnowflake.Missing,
    val channel: Optional<DiscordChannel> = Optional.Missing(),
    @SerialName("channel_id")
    val channelId: OptionalSnowflake = OptionalSnowflake.Missing,
    val member: Optional<DiscordInteractionGuildMember> = Optional.Missing(),
    val user: Optional<DiscordUser> = Optional.Missing(),
    val token: String,
    val version: Int,
    @Serializable(with = MaybeMessageSerializer::class)
    val message: Optional<DiscordMessage> = Optional.Missing(),
    @SerialName("app_permissions")
    val appPermissions: Optional<Permissions> = Optional.Missing(),
    val locale: Optional<Locale> = Optional.Missing(),
    @SerialName("guild_locale")
    val guildLocale: Optional<Locale> = Optional.Missing(),
) {

    /**
     * Serializer that handles incomplete messages in [DiscordInteraction.message]. Discards
     * any incomplete messages as missing optionals.
     */
    private object MaybeMessageSerializer :
        KSerializer<Optional<DiscordMessage>> by Optional.serializer(DiscordMessage.serializer()) {

        override fun deserialize(decoder: Decoder): Optional<DiscordMessage> {
            decoder as JsonDecoder

            val element = decoder.decodeJsonElement().jsonObject

            //check if required fields are present, if not, discard the data
            return if (
                element["channel_id"] == null ||
                element["author"] == null
            ) {
                Optional.Missing()
            } else {
                decoder.json.decodeFromJsonElement(
                    Optional.serializer(DiscordMessage.serializer()), element
                )
            }
        }


    }
}


@Serializable
public data class InteractionCallbackData(
    val id: OptionalSnowflake = OptionalSnowflake.Missing,
    val type: Optional<ApplicationCommandType> = Optional.Missing(),
    @SerialName("target_id")
    val targetId: OptionalSnowflake = OptionalSnowflake.Missing,
    val name: Optional<String> = Optional.Missing(),
    val resolved: Optional<ResolvedObjects> = Optional.Missing(),
    val options: Optional<List<Option>> = Optional.Missing(),
    @SerialName("guild_id")
    val guildId: OptionalSnowflake = OptionalSnowflake.Missing,
    @SerialName("custom_id")
    val customId: Optional<String> = Optional.Missing(),
    @SerialName("component_type")
    val componentType: Optional<ComponentType> = Optional.Missing(),
    val values: Optional<List<String>> = Optional.Missing(),
    val components: Optional<List<DiscordComponent>> = Optional.Missing()
)

@Serializable(with = Option.Serializer::class)
public sealed class Option {
    public abstract val name: String
    public abstract val type: ApplicationCommandOptionType

    internal object Serializer : KSerializer<Option> {

        override val descriptor = buildClassSerialDescriptor("dev.kord.common.entity.Option") {
            element("name", String.serializer().descriptor)
            element("type", ApplicationCommandOptionType.serializer().descriptor)
            element("value", JsonPrimitive.serializer().descriptor, isOptional = true)
            element(
                "options",
                // see https://github.com/Kotlin/kotlinx.serialization/issues/1815
                descriptor = @OptIn(ExperimentalSerializationApi::class) object : SerialDescriptor {
                    private val original get() = recursiveListSerializer.descriptor
                    override val serialName get() = original.serialName
                    override val kind get() = original.kind
                    override val isNullable get() = original.isNullable
                    override val elementsCount get() = original.elementsCount
                    override fun getElementName(index: Int) = original.getElementName(index)
                    override fun getElementIndex(name: String) = original.getElementIndex(name)
                    override fun getElementAnnotations(index: Int) = original.getElementAnnotations(index)
                    override fun getElementDescriptor(index: Int) = original.getElementDescriptor(index)
                    override fun isElementOptional(index: Int) = original.isElementOptional(index)
                },
                isOptional = true,
            )
            element("focused", Boolean.serializer().descriptor, isOptional = true)
        }

        private val recursiveListSerializer = ListSerializer(elementSerializer = this)

        override fun serialize(encoder: Encoder, value: Option) = encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, index = 0, value.name)
            encodeSerializableElement(descriptor, index = 1, ApplicationCommandOptionType.serializer(), value.type)
            when (value) {
                is SubCommand -> value.options.value?.let { options ->
                    encodeSerializableElement(descriptor, index = 3, recursiveListSerializer, options)
                }
                is CommandGroup -> value.options.value?.let { options ->
                    encodeSerializableElement(descriptor, index = 3, recursiveListSerializer, options)
                }
                is CommandArgument<*> -> {
                    when (value) {
                        is CommandArgument.StringArgument -> encodeStringElement(descriptor, index = 2, value.value)
                        is CommandArgument.IntegerArgument -> encodeLongElement(descriptor, index = 2, value.value)
                        is CommandArgument.BooleanArgument -> encodeBooleanElement(descriptor, index = 2, value.value)
                        is CommandArgument.NumberArgument -> encodeDoubleElement(descriptor, index = 2, value.value)
                        is CommandArgument.UserArgument ->
                            encodeSerializableElement(descriptor, index = 2, Snowflake.serializer(), value.value)
                        is CommandArgument.ChannelArgument ->
                            encodeSerializableElement(descriptor, index = 2, Snowflake.serializer(), value.value)
                        is CommandArgument.RoleArgument ->
                            encodeSerializableElement(descriptor, index = 2, Snowflake.serializer(), value.value)
                        is CommandArgument.MentionableArgument ->
                            encodeSerializableElement(descriptor, index = 2, Snowflake.serializer(), value.value)
                        is CommandArgument.AttachmentArgument ->
                            encodeSerializableElement(descriptor, index = 2, Snowflake.serializer(), value.value)
                        is CommandArgument.AutoCompleteArgument ->
                            encodeStringElement(descriptor, index = 2, value.value)
                    }
                    value.focused.value?.let { focused -> encodeBooleanElement(descriptor, index = 4, focused) }
                }
            }
        }

        override fun deserialize(decoder: Decoder) = decoder.decodeStructure(descriptor) {
            var name: String? = null
            var type: ApplicationCommandOptionType? = null
            var value: JsonPrimitive? = null
            var options: List<Option>? = null
            var focused: OptionalBoolean = OptionalBoolean.Missing

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> name = decodeStringElement(descriptor, index)
                    1 -> type = decodeSerializableElement(descriptor, index, ApplicationCommandOptionType.serializer())
                    2 -> value = decodeSerializableElement(descriptor, index, JsonPrimitive.serializer())
                    3 -> options = decodeSerializableElement(descriptor, index, recursiveListSerializer)
                    4 -> focused = OptionalBoolean.Value(decodeBooleanElement(descriptor, index))

                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("Unexpected index: $index")
                }
            }

            @OptIn(ExperimentalSerializationApi::class)
            if (name == null || type == null) throw MissingFieldException(
                missingFields = listOfNotNull("name".takeIf { name == null }, "type".takeIf { type == null }),
                serialName = descriptor.serialName,
            )

            // Discord allows users to put anything into autocomplete, so we cannot convert this with the expected type
            if (
                focused.value == true
                && type != ApplicationCommandOptionType.SubCommand
                && type != ApplicationCommandOptionType.SubCommandGroup
                && type !is ApplicationCommandOptionType.Unknown
            ) {
                CommandArgument.AutoCompleteArgument(name, type, convertValue(value, String.serializer()), focused)
            } else when (type) {
                ApplicationCommandOptionType.SubCommand -> SubCommand(
                    name,
                    options = Optional(options?.map {
                        it as? CommandArgument<*>
                            ?: throw SerializationException("Expected only CommandArguments in 'options'")
                    }).coerceToMissing(),
                )
                ApplicationCommandOptionType.SubCommandGroup -> CommandGroup(
                    name,
                    options = Optional(options?.map {
                        it as? SubCommand ?: throw SerializationException("Expected only SubCommands in 'options'")
                    }).coerceToMissing(),
                )
                ApplicationCommandOptionType.String ->
                    CommandArgument.StringArgument(name, convertValue(value, String.serializer()), focused)
                ApplicationCommandOptionType.Integer ->
                    CommandArgument.IntegerArgument(name, convertValue(value, Long.serializer()), focused)
                ApplicationCommandOptionType.Boolean ->
                    CommandArgument.BooleanArgument(name, convertValue(value, Boolean.serializer()), focused)
                ApplicationCommandOptionType.Number ->
                    CommandArgument.NumberArgument(name, convertValue(value, Double.serializer()), focused)
                ApplicationCommandOptionType.User ->
                    CommandArgument.UserArgument(name, convertValue(value, Snowflake.serializer()), focused)
                ApplicationCommandOptionType.Channel ->
                    CommandArgument.ChannelArgument(name, convertValue(value, Snowflake.serializer()), focused)
                ApplicationCommandOptionType.Role ->
                    CommandArgument.RoleArgument(name, convertValue(value, Snowflake.serializer()), focused)
                ApplicationCommandOptionType.Mentionable ->
                    CommandArgument.MentionableArgument(name, convertValue(value, Snowflake.serializer()), focused)
                ApplicationCommandOptionType.Attachment ->
                    CommandArgument.AttachmentArgument(name, convertValue(value, Snowflake.serializer()), focused)
                is ApplicationCommandOptionType.Unknown -> throw SerializationException("$type is unknown")
            }
        }

        private fun <T : Any> CompositeDecoder.convertValue(
            value: JsonPrimitive?,
            deserializer: DeserializationStrategy<T>,
        ): T {
            @OptIn(ExperimentalSerializationApi::class)
            if (value == null) throw MissingFieldException("value", descriptor.serialName)
            // cast must succeed, JsonPrimitive was decoded successfully
            return (this as JsonDecoder).json.decodeFromJsonElement(deserializer, value)
        }
    }
}

public data class SubCommand(
    override val name: String,
    val options: Optional<List<CommandArgument<@Contextual Any>>> = Optional.Missing()
) : Option() {
    override val type: ApplicationCommandOptionType
        get() = ApplicationCommandOptionType.SubCommand

    @Deprecated("This companion object is deprecated and no longer useful", level = DeprecationLevel.WARNING)
    public companion object {
        @Deprecated(
            "SubCommand is no longer serializable",
            ReplaceWith("Option.serializer()", "dev.kord.common.entity.Option"),
            level = DeprecationLevel.WARNING,
        )
        public fun serializer(): KSerializer<SubCommand> =
            object : KSerializer<SubCommand> {
                override val descriptor get() = Option.serializer().descriptor
                override fun serialize(encoder: Encoder, value: SubCommand) =
                    encoder.encodeSerializableValue(Option.serializer(), value)

                override fun deserialize(decoder: Decoder) =
                    decoder.decodeSerializableValue(Option.serializer()) as SubCommand
            }
    }
}


public sealed class CommandArgument<out T : Any> : Option() {

    public abstract val value: T
    public abstract val focused: OptionalBoolean

    public data class StringArgument(
        override val name: String,
        override val value: String,
        override val focused: OptionalBoolean = OptionalBoolean.Missing
    ) : CommandArgument<String>() {
        override val type: ApplicationCommandOptionType
            get() = ApplicationCommandOptionType.String
    }

    public data class IntegerArgument(
        override val name: String,
        override val value: Long,
        override val focused: OptionalBoolean = OptionalBoolean.Missing
    ) : CommandArgument<Long>() {
        override val type: ApplicationCommandOptionType
            get() = ApplicationCommandOptionType.Integer
    }

    public data class NumberArgument(
        override val name: String,
        override val value: Double,
        override val focused: OptionalBoolean = OptionalBoolean.Missing
    ) : CommandArgument<Double>() {
        override val type: ApplicationCommandOptionType
            get() = ApplicationCommandOptionType.Number
    }

    public data class BooleanArgument(
        override val name: String,
        override val value: Boolean,
        override val focused: OptionalBoolean = OptionalBoolean.Missing
    ) : CommandArgument<Boolean>() {
        override val type: ApplicationCommandOptionType
            get() = ApplicationCommandOptionType.Boolean
    }

    public data class UserArgument(
        override val name: String,
        override val value: Snowflake,
        override val focused: OptionalBoolean = OptionalBoolean.Missing
    ) : CommandArgument<Snowflake>() {
        override val type: ApplicationCommandOptionType
            get() = ApplicationCommandOptionType.User
    }

    public data class ChannelArgument(
        override val name: String,
        override val value: Snowflake,
        override val focused: OptionalBoolean = OptionalBoolean.Missing
    ) : CommandArgument<Snowflake>() {
        override val type: ApplicationCommandOptionType
            get() = ApplicationCommandOptionType.Channel
    }

    public data class RoleArgument(
        override val name: String,
        override val value: Snowflake,
        override val focused: OptionalBoolean = OptionalBoolean.Missing
    ) : CommandArgument<Snowflake>() {
        override val type: ApplicationCommandOptionType
            get() = ApplicationCommandOptionType.Role
    }

    public data class MentionableArgument(
        override val name: String,
        override val value: Snowflake,
        override val focused: OptionalBoolean = OptionalBoolean.Missing
    ) : CommandArgument<Snowflake>() {
        override val type: ApplicationCommandOptionType
            get() = ApplicationCommandOptionType.Mentionable
    }

    public data class AttachmentArgument(
        override val name: String,
        override val value: Snowflake,
        override val focused: OptionalBoolean = OptionalBoolean.Missing
    ) : CommandArgument<Snowflake>() {
        override val type: ApplicationCommandOptionType
            get() = ApplicationCommandOptionType.Attachment
    }

    /**
     * Representation of a partial user input of an auto completed argument.
     *
     * @property name the name of the property
     * @property type the type of the backing argument (not the type of [value] as the user can enter anything)
     * @property value whatever the user already typed into the argument field
     * @property focused always true, since this is an auto complete argument
     */
    public data class AutoCompleteArgument(
        override val name: String,
        override val type: ApplicationCommandOptionType,
        override val value: String,
        override val focused: OptionalBoolean
    ) : CommandArgument<String>()

    @Deprecated("This companion object is deprecated and no longer useful", level = DeprecationLevel.WARNING)
    public companion object {
        @Suppress("UNUSED_PARAMETER", "RemoveRedundantQualifierName")
        @Deprecated(
            "CommandArgument is no longer serializable",
            ReplaceWith("Option.serializer()", "dev.kord.common.entity.Option"),
            level = DeprecationLevel.WARNING,
        )
        public fun serializer(unused: KSerializer<*>): KSerializer<CommandArgument<*>> =
            object : KSerializer<CommandArgument<*>> {
                override val descriptor get() = Option.serializer().descriptor
                override fun serialize(encoder: Encoder, value: CommandArgument<*>) =
                    encoder.encodeSerializableValue(Option.serializer(), value)

                override fun deserialize(decoder: Decoder) =
                    decoder.decodeSerializableValue(Option.serializer()) as CommandArgument<*>
            }
    }
}

public data class CommandGroup(
    override val name: String,
    val options: Optional<List<SubCommand>> = Optional.Missing(),
) : Option() {
    override val type: ApplicationCommandOptionType
        get() = ApplicationCommandOptionType.SubCommandGroup
}

@Serializable
public data class DiscordGuildApplicationCommandPermissions(
    val id: Snowflake,
    @SerialName("application_id")
    val applicationId: Snowflake,
    @SerialName("guild_id")
    val guildId: Snowflake,
    val permissions: List<DiscordGuildApplicationCommandPermission>
)

@Serializable
public data class DiscordGuildApplicationCommandPermission(
    val id: Snowflake,
    val type: ApplicationCommandPermissionType,
    val permission: Boolean
)

@Serializable
public data class DiscordAutoComplete<T>(
    val choices: List<Choice<T>>
)

@Serializable
public data class DiscordModal(
    val title: String,
    @SerialName("custom_id")
    val customId: String,
    val components: List<DiscordComponent>,
)
