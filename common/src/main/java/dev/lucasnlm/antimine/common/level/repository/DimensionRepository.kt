package dev.lucasnlm.antimine.common.level.repository

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.os.Build
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository

interface IDimensionRepository {
    fun areaSize(): Float
    fun areaSizeWithPadding(): Float
    fun areaSeparator(): Float
    fun displaySize(): Size
    fun actionBarSize(): Int
    fun navigationBarHeight(): Int
    fun isRoundDevice(): Boolean
}

data class Size(
    val width: Int,
    val height: Int
)

class DimensionRepository(
    private val context: Context,
    private val preferencesRepository: IPreferencesRepository
) : IDimensionRepository {

    override fun areaSize(): Float = if (preferencesRepository.useLargeAreas()) {
        context.resources.getDimension(R.dimen.accessible_field_size)
    } else {
        context.resources.getDimension(R.dimen.field_size)
    }

    override fun areaSeparator(): Float {
        return context.resources.getDimension(R.dimen.field_padding)
    }

    override fun areaSizeWithPadding(): Float {
        return areaSize() + 2 * areaSeparator()
    }

    override fun displaySize(): Size = with(Resources.getSystem().displayMetrics) {
        return Size(this.widthPixels, this.heightPixels)
    }

    override fun actionBarSize(): Int {
        val styledAttributes: TypedArray =
            context.theme.obtainStyledAttributes(
                IntArray(1) { android.R.attr.actionBarSize }
            )
        val actionBarSize: Int = styledAttributes.getDimension(0, 0.0f).toInt()
        styledAttributes.recycle()
        return actionBarSize
    }

    override fun navigationBarHeight(): Int {
        // TODO use official mode if available
        val resources = context.resources
        val resourceId: Int = resources.getIdentifier(NAVIGATION_BAR_HEIGHT, DEF_TYPE_DIMEN, DEF_PACKAGE)
        return if (resourceId > 0) { resources.getDimensionPixelSize(resourceId) } else 0
    }

    override fun isRoundDevice(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) context.resources.configuration.isScreenRound else false
    }

    companion object {
        private const val NAVIGATION_BAR_HEIGHT = "navigation_bar_height"
        private const val DEF_TYPE_DIMEN = "dimen"
        private const val DEF_PACKAGE = "android"
    }
}
