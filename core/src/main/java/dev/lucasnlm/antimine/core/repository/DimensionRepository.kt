package dev.lucasnlm.antimine.core.repository

import dev.lucasnlm.antimine.core.models.MinefieldSize

interface DimensionRepository {
    fun areaSize(): Float

    fun areaSizeWithPadding(): Float

    fun areaSeparator(): Float

    fun displaySize(): MinefieldSize

    fun actionBarSizeWithStatus(): Int

    fun actionBarSize(): Int

    fun navigationBarHeight(): Int

    fun verticalNavigationBarHeight(): Int

    fun horizontalNavigationBarHeight(): Int
}
