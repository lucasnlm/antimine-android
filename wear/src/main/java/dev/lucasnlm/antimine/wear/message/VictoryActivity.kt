package dev.lucasnlm.antimine.wear.message

import dev.lucasnlm.antimine.wear.R
import dev.lucasnlm.antimine.i18n.R as i18n

class VictoryActivity(
    override val message: Int = i18n.string.you_won,
    override val emojiRes: Int = R.drawable.emoji_smiling_face_with_sunglasses,
) : EmojiMessageActivity()
