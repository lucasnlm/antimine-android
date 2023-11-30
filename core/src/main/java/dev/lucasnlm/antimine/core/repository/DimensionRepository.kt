package dev.lucasnlm.antimine.core.repository

import android.util.DisplayMetrics

interface DimensionRepository {
    fun areaSize(): Float

    fun areaSizeWithPadding(): Float

    fun areaSeparator(): Float

    fun displayMetrics(): DisplayMetrics

    fun actionBarSizeWithStatus(): Int

    fun actionBarSize(): Int

    fun navigationBarHeight(): Int

    fun verticalNavigationBarHeight(): Int

    fun horizontalNavigationBarHeight(): Int
}
