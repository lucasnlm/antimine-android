package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.core.repository.Size

class FixedDimensionRepository : IDimensionRepository {
    override fun areaSize(): Float = 50.0f

    override fun areaSizeWithPadding(): Float {
        return areaSize() + 2 * areaSeparator()
    }

    override fun defaultAreaSize(): Float = 50.0f

    override fun areaSeparator(): Float = 1.0f

    override fun displaySize(): Size = Size(50 * 15, 50 * 30)

    override fun actionBarSize(): Int = 50

    override fun navigationBarHeight(): Int = 0
}
