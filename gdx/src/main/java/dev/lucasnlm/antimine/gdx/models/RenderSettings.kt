package dev.lucasnlm.antimine.gdx.models

import dev.lucasnlm.antimine.ui.model.AppTheme

data class RenderSettings(
    val theme: AppTheme,
    val areaSize: Float,
    val internalPadding: InternalPadding,
    val navigationBarHeight: Float,
    val appBarWithStatusHeight: Float,
    val appBarHeight: Float,
    val radius: Float,
    val joinAreas: Boolean,
    val squareDivider: Float,
    val quality: RenderQuality,
)
