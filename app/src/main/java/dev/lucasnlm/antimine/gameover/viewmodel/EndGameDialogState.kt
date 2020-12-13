package dev.lucasnlm.antimine.gameover.viewmodel

import androidx.annotation.DrawableRes

data class EndGameDialogState(
    @DrawableRes val titleEmoji: Int,
    val title: String,
    val message: String,
    val isVictory: Boolean?,
    val showContinueButton: Boolean,
    val received: Int
)
