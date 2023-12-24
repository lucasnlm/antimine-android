package dev.lucasnlm.antimine.ui.model

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

/**
 * A class that represents a skin for the app.
 * @property id The id of the skin.
 * @property file The file name of the skin.
 * @property thumbnailImageRes The resource id of the skin.
 * @property canTint Whether the skin can be tinted.
 * @property isPremium Whether the skin is premium.
 * @property joinAreas Whether the skin has padding.
 * @property background The background of the skin.
 * @property showPadding Whether the skin should show padding.
 */
data class AppSkin(
    val id: Long,
    val file: String,
    @DrawableRes val thumbnailImageRes: Int,
    val canTint: Boolean,
    val isPremium: Boolean,
    val joinAreas: Boolean,
    val background: Int = 0,
    val showPadding: Boolean = true,
    val backgroundOnAll: Boolean = false,
    @ColorInt val forceBackground: Int? = null,
)
