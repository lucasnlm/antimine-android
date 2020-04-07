package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.Size

class MockDimensionRepository : IDimensionRepository {
    override fun areaSize(): Float = 50.0f

    override fun displaySize(): Size = Size(50 * 20, 50 * 30)

    override fun actionBarSize(): Int = 50
}
