package dev.lucasnlm.antimine.wear.message

import dev.lucasnlm.antimine.wear.R

class GameOverActivity(
    override val message: Int = R.string.you_lost,
    override val emojiRes: Int = R.drawable.emoji_bomb,
) : EmojiMessageActivity()
