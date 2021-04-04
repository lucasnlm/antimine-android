package dev.lucasnlm.antimine.gameover.viewmodel

import androidx.annotation.DrawableRes
import dev.lucasnlm.antimine.gameover.model.GameResult

sealed class EndGameDialogEvent {
    data class BuildCustomEndGame(
        val gameResult: GameResult,
        val showContinueButton: Boolean,
        val time: Long,
        val rightMines: Int,
        val totalMines: Int,
        val received: Int,
        val turn: Int,
    ) : EndGameDialogEvent()

    data class ChangeEmoji(
        val gameResult: GameResult,
        @DrawableRes val titleEmoji: Int,
    ) : EndGameDialogEvent()
}
