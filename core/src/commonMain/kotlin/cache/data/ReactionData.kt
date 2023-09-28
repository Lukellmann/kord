package dev.kord.core.cache.data

import dev.kord.common.Color
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Reaction
import dev.kord.common.entity.ReactionCountDetails
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
public data class ReactionData(
    val count: Int,
    val countDetails: ReactionCountDetails,
    val me: Boolean,
    val meBurst: Boolean,
    val emojiId: Snowflake? = null,
    val emojiName: String? = null,
    val emojiAnimated: Boolean,
    val burstColors: List<Color>,
) {
    public companion object {
        public fun from(entity: Reaction): ReactionData = with(entity) {
            ReactionData(
                count = count,
                countDetails = countDetails,
                me = me,
                meBurst = meBurst,
                emojiId = emoji.id,
                emojiName = emoji.name,
                emojiAnimated = emoji.animated.orElse(false),
                burstColors = burstColors,
            )
        }

        public fun from(count: Int, me: Boolean, entity: DiscordPartialEmoji): ReactionData = with(entity) {
            // TODO
            ReactionData(
                count = count,
                countDetails = ReactionCountDetails(burst = 0, normal = count),
                me = me,
                meBurst = false,
                emojiId = id,
                emojiName = name,
                emojiAnimated = animated.orElse(false),
                burstColors = emptyList(),
            )
        }
    }
}
