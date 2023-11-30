package dev.lucasnlm.antimine.mocks

import android.util.DisplayMetrics
import dev.lucasnlm.antimine.core.repository.DimensionRepository

class FixedDimensionRepository : DimensionRepository {
    override fun areaSize(): Float = 50.0f

    override fun areaSizeWithPadding(): Float {
        return areaSize() + 2 * areaSeparator()
    }

    override fun areaSeparator(): Float = 1.0f

    override fun displayMetrics(): DisplayMetrics {
        return DisplayMetrics().apply {
            widthPixels = 50 * 15
            heightPixels = 50 * 30
        }
    }

    override fun actionBarSizeWithStatus(): Int = 50

    override fun actionBarSize(): Int = 50

    override fun navigationBarHeight(): Int = 0

    override fun verticalNavigationBarHeight(): Int = 0

    override fun horizontalNavigationBarHeight(): Int = 0
}
