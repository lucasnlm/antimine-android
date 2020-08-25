package dev.lucasnlm.antimine.level.viewmodel

import androidx.annotation.DrawableRes

data class EndGameDialogState(
    @DrawableRes val titleEmoji: Int,
    val title: String,
    val message: String,
    val isVictory: Boolean?,
)
