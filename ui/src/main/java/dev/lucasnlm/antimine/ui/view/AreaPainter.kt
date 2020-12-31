package dev.lucasnlm.antimine.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.content.ContextCompat
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.AreaPaintSettings
import dev.lucasnlm.antimine.core.models.Mark
import dev.lucasnlm.antimine.ui.R
import dev.lucasnlm.antimine.ui.model.AppTheme

fun Area.paintOnCanvas(
    context: Context,
    canvas: Canvas,
    isAmbientMode: Boolean,
    isLowBitAmbient: Boolean,
    isFocused: Boolean,
    paintSettings: AreaPaintSettings,
    theme: AppTheme,
    markPadding: Int? = null,
    minePadding: Int? = null,
) {
    paintSettings.run {
        if (isCovered) {
            if (isAmbientMode) {
                painter.apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2.0f
                    isAntiAlias = !isLowBitAmbient
                    color = 0xFFFFFF
                    alpha = 0x66
                }
            } else {
                painter.apply {
                    style = Paint.Style.FILL
                    isAntiAlias = !isLowBitAmbient
                    color = if (posY % 2 == 0) {
                        if (posX % 2 == 0) theme.palette.covered else theme.palette.coveredOdd
                    } else {
                        if (posX % 2 == 0) theme.palette.coveredOdd else theme.palette.covered
                    }
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
                        ContextCompat.getDrawable(context, theme.assets.wrongFlag)
                    } else {
                        ContextCompat.getDrawable(context, theme.assets.flag)
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
                    val question = ContextCompat.getDrawable(context, theme.assets.questionMark)

                    question?.setBounds(
                        rectF.left.toInt(),
                        rectF.top.toInt(),
                        rectF.right.toInt(),
                        rectF.bottom.toInt()
                    )
                    question?.draw(canvas)
                }
                else -> {
                    if (revealed) {
                        val padding = minePadding ?: context.resources.getDimension(R.dimen.mine_padding).toInt()

                        val revealedDrawable = ContextCompat.getDrawable(context, theme.assets.revealed)

                        revealedDrawable?.setBounds(
                            rectF.left.toInt() + padding,
                            rectF.top.toInt() + padding,
                            rectF.right.toInt() - padding,
                            rectF.bottom.toInt() - padding
                        )
                        revealedDrawable?.draw(canvas)
                    }
                }
            }
        } else {
            if (isAmbientMode) {
                painter.apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 0.5f
                    isAntiAlias = !isLowBitAmbient
                    color = theme.palette.border
                    alpha = 0xff
                }
            } else {
                painter.apply {
                    style = Paint.Style.FILL
                    isAntiAlias = !isLowBitAmbient
                    color = theme.palette.uncovered
                    color = if (posY % 2 == 0) {
                        if (posX % 2 == 0) theme.palette.uncovered else theme.palette.uncoveredOdd
                    } else {
                        if (posX % 2 == 0) theme.palette.uncoveredOdd else theme.palette.uncovered
                    }
                    alpha = 0xff
                }
            }

            painter.run {
                canvas.drawRoundRect(rectF, radius, radius, this)
            }

            if (hasMine) {
                val padding = minePadding ?: context.resources.getDimension(R.dimen.mine_padding).toInt()

                val mine = when {
                    isAmbientMode -> ContextCompat.getDrawable(context, theme.assets.mineLow)
                    mistake -> ContextCompat.getDrawable(context, theme.assets.mineExploded)
                    else -> ContextCompat.getDrawable(context, theme.assets.mine)
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
                    color = if (isAmbientMode) {
                        0xFFFFFF
                    } else {
                        when (minesAround) {
                            1 -> theme.palette.minesAround1
                            2 -> theme.palette.minesAround2
                            3 -> theme.palette.minesAround3
                            4 -> theme.palette.minesAround4
                            5 -> theme.palette.minesAround5
                            6 -> theme.palette.minesAround6
                            7 -> theme.palette.minesAround7
                            8 -> theme.palette.minesAround8
                            else -> 0x00
                        }
                    }
                    isAntiAlias = !isAmbientMode
                    style = Paint.Style.FILL
                    alpha = 0xFF
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
                    color = theme.palette.highlight
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
                color = theme.palette.focus
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
