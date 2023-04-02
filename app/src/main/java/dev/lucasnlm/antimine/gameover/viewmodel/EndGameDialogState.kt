package dev.lucasnlm.antimine.gameover.viewmodel

import androidx.annotation.DrawableRes
import dev.lucasnlm.antimine.gameover.model.GameResult

data class EndGameDialogState(
    @DrawableRes val titleEmoji: Int,
    val title: String,
    val message: String,
    val gameResult: GameResult,
    val showContinueButton: Boolean,
    val received: Int,
    val showTutorial: Boolean,
    val showMusicDialog: Boolean,
)
