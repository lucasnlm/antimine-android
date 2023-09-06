package dev.lucasnlm.antimine.wear.message

import dev.lucasnlm.antimine.wear.R
import dev.lucasnlm.antimine.i18n.R as i18n

class GameOverActivity(
    override val message: Int = i18n.string.you_lost,
    override val emojiRes: Int = R.drawable.emoji_bomb,
) : EmojiMessageActivity()
