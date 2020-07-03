package dev.lucasnlm.antimine.level.viewmodel

import android.content.Context
import android.graphics.Paint
import android.os.Build
import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.R

class EngGameDialogViewModel : ViewModel() {
    private val paint: Paint = Paint()

    private fun checkGlyphAvailability(glyph: String): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            paint.hasGlyph(glyph)
        } else {
            true
        }
    }

    private fun List<String>.safeRandomEmoji(except: String? = null, fallback: String = "\uD83D\uDCA3") =
        this.filter { it != except && checkGlyphAvailability(it) }
            .ifEmpty { listOf(fallback) }
            .random()

    fun randomVictoryEmoji(except: String? = null) = listOf(
        "\uD83D\uDE00", "\uD83D\uDE0E", "\uD83D\uDE1D", "\uD83E\uDD73", "\uD83D\uDE06"
    ).safeRandomEmoji(except)

    fun randomNeutralEmoji(except: String? = null) = listOf(
        "\uD83D\uDE01", "\uD83E\uDD14", "\uD83D\uDE42", "\uD83D\uDE09"
    ).safeRandomEmoji(except)

    fun randomGameOverEmoji(except: String? = null) = listOf(
        "\uD83D\uDE10", "\uD83D\uDE44", "\uD83D\uDE25", "\uD83D\uDE13", "\uD83D\uDE31",
        "\uD83E\uDD2C", "\uD83E\uDD15", "\uD83D\uDE16", "\uD83D\uDCA3", "\uD83D\uDE05"
    ).safeRandomEmoji(except)

    fun messageTo(context: Context, time: Long, isVictory: Boolean): String =
        if (time != 0L) {
            when {
                isVictory -> context.getString(R.string.game_over_desc_4, time)
                else -> context.getString(R.string.game_over_desc_1)
            }
        } else {
            context.getString(R.string.game_over_desc_1)
        }
}
