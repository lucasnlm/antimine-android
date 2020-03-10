package dev.lucasnlm.antimine.level.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.R

class EngGameDialogViewModel : ViewModel() {
    fun randomVictoryEmoji(except: String? = null) = listOf(
        "\uD83D\uDE00", "\uD83D\uDE0E", "\uD83D\uDE1D", "\uD83E\uDD73", "\uD83D\uDE06"
    ).filter { it != except }.random()

    fun randomGameOverEmoji(except: String? = null) = listOf(
        "\uD83D\uDE10", "\uD83D\uDE44", "\uD83D\uDE25", "\uD83D\uDE13", "\uD83D\uDE31",
        "\uD83E\uDD2C", "\uD83E\uDD15", "\uD83D\uDE16", "\uD83D\uDCA3", "\uD83D\uDE05"
    ).filter { it != except }.random()

    fun messageTo(context: Context, rightMines: Int, totalMines: Int, time: Long, isVictory: Boolean): String =
        if (totalMines != 0 && time != 0L) {
            when {
                isVictory -> context.getString(R.string.game_over_desc_4, time)
                rightMines / totalMines > 0.9 -> context.getString(R.string.game_over_desc_3)
                rightMines < 4 -> context.getString(
                    arrayOf(
                        R.string.game_over_desc_0,
                        R.string.game_over_desc_1
                    ).random()
                )
                else -> context.getString(R.string.game_over_desc_2, rightMines, totalMines, time)
            }
        } else {
            context.getString(R.string.game_over_desc_1)
        }
}
