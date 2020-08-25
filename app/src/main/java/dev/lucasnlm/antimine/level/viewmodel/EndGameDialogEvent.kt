package dev.lucasnlm.antimine.level.viewmodel

import androidx.annotation.DrawableRes

sealed class EndGameDialogEvent {
    data class BuildCustomEndGame(
        val isVictory: Boolean?,
        val time: Long,
        val rightMines: Int,
        val totalMines: Int,
    ) : EndGameDialogEvent()

    data class ChangeEmoji(
        val isVictory: Boolean?,
        @DrawableRes val titleEmoji: Int,
    ) : EndGameDialogEvent()
}
