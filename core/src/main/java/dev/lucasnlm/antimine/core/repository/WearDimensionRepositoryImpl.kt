package dev.lucasnlm.antimine.core.repository

import android.content.Context
import android.content.res.Resources
import dev.lucasnlm.antimine.core.R
import dev.lucasnlm.antimine.core.models.MinefieldSize

class WearDimensionRepositoryImpl(
    private val context: Context,
) : DimensionRepository {

    override fun areaSize(): Float {
        return displaySize().width / 5.0f
    }

    override fun areaSeparator(): Float {
        return context.resources.getDimension(R.dimen.field_padding)
    }

    override fun areaSizeWithPadding(): Float {
        return areaSize() + 2 * areaSeparator()
    }

    override fun displaySize(): MinefieldSize =
        with(Resources.getSystem().displayMetrics) {
            return MinefieldSize(this.widthPixels, this.heightPixels)
        }

    override fun actionBarSizeWithStatus(): Int {
        return 0
    }

    override fun actionBarSize(): Int {
        return 0
    }

    override fun navigationBarHeight(): Int {
        return 0
    }

    override fun verticalNavigationBarHeight(): Int {
        return 0
    }

    override fun horizontalNavigationBarHeight(): Int {
        return 0
    }
}
