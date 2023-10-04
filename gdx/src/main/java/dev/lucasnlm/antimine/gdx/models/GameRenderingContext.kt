package dev.lucasnlm.antimine.gdx.models

import dev.lucasnlm.antimine.ui.model.AppSkin
import dev.lucasnlm.antimine.ui.model.AppTheme

/**
 * Holds the rendering context for the game.
 * @property theme The current theme.
 * @property areaSize The size of each area.
 * @property internalPadding The internal padding of the game.
 * @property navigationBarHeight The height of the navigation bar.
 * @property appBarWithStatusHeight The height of the app bar with status bar.
 * @property appBarHeight The height of the app bar.
 * @property joinAreas Whether areas should be joined.
 * @property appSkin The current skin.
 */
data class GameRenderingContext(
    val theme: AppTheme,
    val areaSize: Float,
    val internalPadding: InternalPadding,
    val navigationBarHeight: Float,
    val appBarWithStatusHeight: Float,
    val appBarHeight: Float,
    val joinAreas: Boolean,
    val appSkin: AppSkin,
)
