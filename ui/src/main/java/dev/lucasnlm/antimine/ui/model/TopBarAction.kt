package dev.lucasnlm.antimine.ui.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

class TopBarAction(
    @StringRes val actionName: Int,
    @DrawableRes val icon: Int,
    val action: () -> Unit,
)
