package dev.lucasnlm.antimine.ui.model

import androidx.annotation.DrawableRes

data class Assets(
    @DrawableRes val wrongFlag: Int,
    @DrawableRes val flag: Int,
    @DrawableRes val questionMark: Int,
    @DrawableRes val toolbarMine: Int,
    @DrawableRes val mine: Int,
    @DrawableRes val mineExploded: Int,
    @DrawableRes val mineLow: Int,
    @DrawableRes val revealed: Int,
)
