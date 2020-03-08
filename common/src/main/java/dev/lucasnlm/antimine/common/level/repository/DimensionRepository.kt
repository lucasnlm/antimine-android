package dev.lucasnlm.antimine.common.level.repository

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.util.DisplayMetrics
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository

interface IDimensionRepository {
    fun areaSize(): Float
    fun displaySize(): DisplayMetrics
    fun actionBarSize(): Int
}

class DimensionRepository(
    private val context: Context,
    private val preferencesRepository: IPreferencesRepository
) : IDimensionRepository {

    override fun areaSize(): Float = if (preferencesRepository.useLargeAreas()) {
        context.resources.getDimension(R.dimen.accessible_field_size)
    } else {
        context.resources.getDimension(R.dimen.field_size)
    }

    override fun displaySize(): DisplayMetrics = Resources.getSystem().displayMetrics

    override fun actionBarSize(): Int {
        val styledAttributes: TypedArray =
            context.theme.obtainStyledAttributes(
                IntArray(1) { android.R.attr.actionBarSize }
            )
        val actionBarSize: Int = styledAttributes.getDimension(0, 0.0f).toInt()
        styledAttributes.recycle()
        return actionBarSize
    }
}
