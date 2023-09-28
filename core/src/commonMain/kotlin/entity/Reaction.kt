package dev.kord.core.entity

import dev.kord.common.Color
import dev.kord.common.entity.ReactionCountDetails
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.KordObject
import dev.kord.core.cache.data.ReactionData

/**
 * An instance of a [Discord Reaction](https://discord.com/developers/docs/resources/channel#reaction-object).
 */
public class Reaction(public val data: ReactionData, override val kord: Kord) : KordObject {

    public val id: Snowflake? get() = data.emojiId

    /** The total number of times this emoji has been used to react (including super reactions). */
    public val count: Int get() = data.count

    public val countDetails: ReactionCountDetails get() = data.countDetails

    /**
     * Whether the current user reacted to the message with this emoji.
     */
    public val selfReacted: Boolean get() = data.me

    /** Whether the current user super-reacted to the message with this emoji. */
    public val selfSuperReacted: Boolean get() = data.meBurst

    /**
     * The emoji of this reaction.
     */
    public val emoji: ReactionEmoji
        get() = when (data.emojiId) {
            null -> ReactionEmoji.Unicode(data.emojiName!!)
            else -> ReactionEmoji.Custom(data.emojiId, data.emojiName ?: "", data.emojiAnimated)
        }

    /** The colors used for super reaction. */
    public val superReactionColors: List<Color> get() = data.burstColors

    /**
     * Whether the emoji is animated.
     */
    public val isAnimated: Boolean get() = data.emojiAnimated

    override fun toString(): String {
        return "Reaction(data=$data, kord=$kord)"
    }

}
