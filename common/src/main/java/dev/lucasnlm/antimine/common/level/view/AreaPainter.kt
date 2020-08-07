package dev.lucasnlm.antimine.common.level.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.content.ContextCompat
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.AreaPaintSettings
import dev.lucasnlm.antimine.common.level.models.Mark
import dev.lucasnlm.antimine.core.themes.model.AppTheme

fun Area.paintOnCanvas(
    context: Context,
    canvas: Canvas,
    isAmbientMode: Boolean,
    isLowBitAmbient: Boolean,
    isFocused: Boolean,
    paintSettings: AreaPaintSettings,
    appTheme: AppTheme,
    markPadding: Int? = null,
    minePadding: Int? = null
) {
    paintSettings.run {
        if (isCovered) {
            if (isAmbientMode) {
                painter.apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2.0f
                    isAntiAlias = !isLowBitAmbient
                    color = appTheme.palette.border
                    alpha = 0xff
                }
            } else {
                painter.apply {
                    style = Paint.Style.FILL
                    isAntiAlias = !isLowBitAmbient
                    color = appTheme.palette.covered
                    alpha = if (highlighted) 155 else 255
                }
            }

            painter.run {
                canvas.drawRoundRect(rectF, radius, radius, this)
            }

            when (mark) {
                Mark.Flag -> {
                    val padding = markPadding ?: context.resources.getDimension(R.dimen.mark_padding).toInt()

                    val flag = if (mistake) {
                        ContextCompat.getDrawable(context, R.drawable.red_flag)
                    } else {
                        ContextCompat.getDrawable(context, R.drawable.flag)
                    }

                    flag?.setBounds(
                        rectF.left.toInt() + padding,
                        rectF.top.toInt() + padding,
                        rectF.right.toInt() - padding,
                        rectF.bottom.toInt() - padding
                    )
                    flag?.draw(canvas)
                }
                Mark.Question -> {
                    val question = ContextCompat.getDrawable(context, R.drawable.question)

                    question?.setBounds(
                        rectF.left.toInt(),
                        rectF.top.toInt(),
                        rectF.right.toInt(),
                        rectF.bottom.toInt()
                    )
                    question?.draw(canvas)
                }
                else -> {}
            }
        } else {
            if (isAmbientMode) {
                painter.apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 0.5f
                    isAntiAlias = !isLowBitAmbient
                    color = appTheme.palette.border
                    alpha = 0xff
                }
            } else {
                painter.apply {
                    style = Paint.Style.FILL
                    isAntiAlias = !isLowBitAmbient
                    color = appTheme.palette.uncovered
                    alpha = 0xff
                }
            }

            painter.run {
                canvas.drawRoundRect(rectF, radius, radius, this)
            }

            if (hasMine) {
                val padding = minePadding ?: context.resources.getDimension(R.dimen.mine_padding).toInt()

                val mine = when {
                    isAmbientMode -> ContextCompat.getDrawable(context, R.drawable.mine_low)
                    mistake -> ContextCompat.getDrawable(context, R.drawable.mine_exploded)
                    else -> ContextCompat.getDrawable(context, R.drawable.mine)
                }

                mine?.setBounds(
                    rectF.left.toInt() + padding,
                    rectF.top.toInt() + padding,
                    rectF.right.toInt() - padding,
                    rectF.bottom.toInt() - padding
                )
                mine?.draw(canvas)
            } else if (minesAround > 0) {
                painter.apply {
                    color = when (minesAround) {
                        1 -> appTheme.palette.minesAround1
                        2 -> appTheme.palette.minesAround2
                        3 -> appTheme.palette.minesAround3
                        4 -> appTheme.palette.minesAround4
                        5 -> appTheme.palette.minesAround5
                        6 -> appTheme.palette.minesAround6
                        7 -> appTheme.palette.minesAround7
                        8 -> appTheme.palette.minesAround8
                        else -> 0x00
                    }
                    alpha = 0xff
                }
                canvas.drawText(minesAround.toString(), paintSettings, painter)
            }

            if (highlighted) {
                val highlightWidth = context.resources.getDimension(R.dimen.highlight_stroke)
                val halfWidth = highlightWidth * 0.5f

                painter.apply {
                    style = Paint.Style.STROKE
                    strokeWidth = highlightWidth
                    isAntiAlias = !isLowBitAmbient
                    color = appTheme.palette.highlight
                    alpha = 0xff

                    val rect = RectF(
                        rectF.left + halfWidth,
                        rectF.top + halfWidth,
                        rectF.right - halfWidth,
                        rectF.bottom - halfWidth
                    )

                    canvas.drawRoundRect(rect, radius, radius, this)
                }
            }
        }

        if (isFocused) {
            val highlightWidth = context.resources.getDimension(R.dimen.highlight_stroke)
            val halfWidth = highlightWidth * 0.5f

            painter.apply {
                style = Paint.Style.STROKE
                strokeWidth = highlightWidth
                isAntiAlias = !isLowBitAmbient
                color = appTheme.palette.focus
                alpha = 0xff
            }

            val rect = RectF(
                rectF.left + halfWidth,
                rectF.top + halfWidth,
                rectF.right - halfWidth,
                rectF.bottom - halfWidth
            )

            canvas.drawRoundRect(rect, radius, radius * 0.25f, painter)
        }
    }
}

private fun Canvas.drawText(text: String, paintSettings: AreaPaintSettings, paint: Paint) {
    paintSettings.run {
        val bounds = Rect()
        paint.getTextBounds(text.toCharArray(), 0, 1, bounds)
        paint.textSize = rectF.height() * 0.45f

        val xPos = rectF.width() * 0.5f
        val yPos = (bounds.height() + rectF.height()) * 0.5f

        drawText(text, xPos, yPos, paint)
    }
}
