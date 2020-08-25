package dev.lucasnlm.antimine.playgames.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.lucasnlm.antimine.playgames.viewmodel.PlayGamesEvent

data class PlayGamesItem(
    val id: Int,
    @DrawableRes val iconRes: Int,
    @StringRes val stringRes: Int,
    val triggerEvent: PlayGamesEvent,
)
