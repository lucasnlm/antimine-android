package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.core.models.MinefieldSize
import dev.lucasnlm.antimine.core.repository.DimensionRepository

class FixedDimensionRepository : DimensionRepository {
    override fun areaSize(): Float = 50.0f

    override fun areaSizeWithPadding(): Float {
        return areaSize() + 2 * areaSeparator()
    }

    override fun areaSeparator(): Float = 1.0f

    override fun displaySize(): MinefieldSize = MinefieldSize(50 * 15, 50 * 30)

    override fun actionBarSizeWithStatus(): Int = 50

    override fun actionBarSize(): Int = 50

    override fun navigationBarHeight(): Int = 0

    override fun verticalNavigationBarHeight(): Int = 0

    override fun horizontalNavigationBarHeight(): Int = 0
}
