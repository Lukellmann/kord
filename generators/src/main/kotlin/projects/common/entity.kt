package dev.kord.generators.projects.common

import dev.kord.generators.dsl.KordEnum
import dev.kord.generators.dsl.KordEnum.ValuesPropertyType.SET
import dev.kord.generators.dsl.Packages
import kotlin.DeprecationLevel.HIDDEN

fun Packages.commonEntity() = "dev.kord.common.entity" {

    "MessageType" intKordEnum {
        docUrl = "https://discord.com/developers/docs/resources/channel#message-object-message-types"
        valueName = "code"
        // had `public val values: Set<MessageType>` in companion before -> replace with `entries`
        valuesPropertyName = "values"
        valuesPropertyType = SET

        "Default"(0)
        "RecipientAdd"(1)
        "RecipientRemove"(2)
        "Call"(3)
        "ChannelNameChange"(4)
        "ChannelIconChange"(5)
        "ChannelPinnedMessage"(6)
        "UserJoin"(7)
        "GuildBoost"(8)
        "GuildBoostTier1"(9)
        "GuildBoostTier2"(10)
        "GuildBoostTier3"(11)
        "ChannelFollowAdd"(12)
        "GuildDiscoveryDisqualified"(14)
        "GuildDiscoveryRequalified"(15)
        "GuildDiscoveryGracePeriodInitialWarning"(16)
        "GuildDiscoveryGracePeriodFinalWarning"(17)
        "ThreadCreated"(18)
        "Reply"(19)
        "ChatInputCommand"(20)
        "ThreadStarterMessage"(21)
        "GuildInviteReminder"(22)
        "ContextMenuCommand"(23)
        "AutoModerationAction"(24)
        "RoleSubscriptionPurchase"(25)
        "InteractionPremiumUpsell"(26)
        "StageStart"(27)
        "StageEnd"(28)
        "StageSpeaker"(29)
        "StageTopic"(31)
        "GuildApplicationPremiumSubscription"(32)

        fun KordEnum.Entry<*>.renamedTo(replacement: String) {
            kDoc = "@suppress"
            deprecated = Deprecated(
                "Renamed to '$replacement'.", level = HIDDEN,
                replaceWith = ReplaceWith(replacement, "dev.kord.common.entity.MessageType.$replacement"),
            )
        }
        "GuildMemberJoin"(7) { renamedTo("UserJoin") }
        "UserPremiumGuildSubscription"(8) { renamedTo("GuildBoost") }
        "UserPremiumGuildSubscriptionTierOne"(9) { renamedTo("GuildBoostTier1") }
        "UserPremiumGuildSubscriptionTwo"(10) { renamedTo("GuildBoostTier2") }
        "UserPremiumGuildSubscriptionThree"(11) { renamedTo("GuildBoostTier3") }
    }
}
