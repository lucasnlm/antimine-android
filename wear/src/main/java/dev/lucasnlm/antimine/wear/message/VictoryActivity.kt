package dev.lucasnlm.antimine.wear.message

import dev.lucasnlm.antimine.wear.R

class VictoryActivity(
    override val message: Int = R.string.you_won,
    override val emojiRes: Int = R.drawable.emoji_smiling_face_with_sunglasses,
) : EmojiMessageActivity()
