package dev.lucasnlm.antimine.ui.view

import android.content.Context
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import dev.lucasnlm.antimine.core.R
import dev.lucasnlm.antimine.core.models.AreaPaintSettings

fun createAreaPaintSettings(context: Context, size: Float, squareRadius: Int): AreaPaintSettings {
    val resources = context.resources
    return AreaPaintSettings(
        Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL
            textSize = 18.0f * context.resources.displayMetrics.density
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        },
        RectF(0.0f, 0.0f, size, size),
        resources.getDimension(R.dimen.field_radius) * squareRadius
    )
}
