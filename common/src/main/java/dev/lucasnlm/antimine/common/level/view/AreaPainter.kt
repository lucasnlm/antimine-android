package dev.lucasnlm.antimine.common.level.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.data.Area
import dev.lucasnlm.antimine.common.level.data.Mark
import dev.lucasnlm.antimine.common.level.repository.DrawableRepository

fun Area.paintOnCanvas(context: Context,
                       canvas: Canvas,
                       isAmbientMode: Boolean,
                       isLowBitAmbient: Boolean,
                       isFocused: Boolean,
                       drawableRepository: DrawableRepository,
                       paintSettings: AreaPaintSettings,
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
                    color = ContextCompat.getColor(context, android.R.color.white)
                }
            } else {
                painter.apply {
                    style = Paint.Style.FILL
                    isAntiAlias = !isLowBitAmbient
                    color = ContextCompat.getColor(context, R.color.view_cover)
                    alpha = if(highlighted) 155 else 255
                }
            }

            painter.run {
                canvas.drawRoundRect(rectF, radius, radius, this)
            }

            when (mark) {
                Mark.Flag -> {
                    val padding = markPadding ?: context.resources.getDimension(R.dimen.mark_padding).toInt()

                    val flag = if (mistake) {
                        drawableRepository.provideRedFlagDrawable(context)
                    } else {
                        drawableRepository.provideFlagDrawable(context)
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
                    val question = drawableRepository.provideQuestionDrawable(context)

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
                    color = ContextCompat.getColor(context, android.R.color.white)
                }
            } else {
                painter.apply {
                    style = Paint.Style.FILL
                    isAntiAlias = !isLowBitAmbient
                    color = ContextCompat.getColor(context, R.color.view_clean)
                }
            }

            painter.run {
                canvas.drawRoundRect(rectF, radius, radius, this)
            }

            if (hasMine) {
                val padding = minePadding ?: context.resources.getDimension(R.dimen.mine_padding).toInt()

                val mine = when {
                    isAmbientMode -> drawableRepository.provideMineLow(context)
                    mistake -> drawableRepository.provideMineExploded(context)
                    else -> drawableRepository.provideMine(context)
                }

                mine?.setBounds(
                    rectF.left.toInt() + padding,
                    rectF.top.toInt() + padding,
                    rectF.right.toInt() - padding,
                    rectF.bottom.toInt() - padding
                )
                mine?.draw(canvas)
            } else if (minesAround > 0) {
                val color = if (isAmbientMode) {
                    R.color.ambient_color_white
                } else {
                    when (minesAround) {
                        1 -> R.color.mines_arround_1
                        2 -> R.color.mines_arround_2
                        3 -> R.color.mines_arround_3
                        4 -> R.color.mines_arround_4
                        5 -> R.color.mines_arround_5
                        6 -> R.color.mines_arround_6
                        7 -> R.color.mines_arround_7
                        else -> R.color.mines_arround_8
                    }
                }

                painter.color = ContextCompat.getColor(context, color)
                drawText(canvas, painter, minesAround.toString(), paintSettings)
            }

            if (highlighted) {
                painter.apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2.0f
                    isAntiAlias = !isLowBitAmbient
                    color = if (isAmbientMode) {
                        ContextCompat.getColor(context, R.color.white)
                    } else {
                        ContextCompat.getColor(context, R.color.highlight)
                    }

                    canvas.drawRoundRect(rectF, radius, radius, this)
                }
            }
        }

        if (isFocused) {
            painter.apply {
                style = Paint.Style.STROKE
                strokeWidth = 4f
                isAntiAlias = !isLowBitAmbient
                color = ContextCompat.getColor(context, android.R.color.holo_orange_dark)
            }

            canvas.drawRoundRect(rectF, radius, radius, painter)
        }
    }
}

private fun drawText(canvas: Canvas, paint: Paint, text: String, paintSettings: AreaPaintSettings) {
    paintSettings.run {
        val bounds = RectF(rectF).apply {
            right = paint.measureText(text, 0, text.length)
            bottom = paint.descent() - paint.ascent()
            left += (rectF.width() - right) / 2.0f
            top += (rectF.height() - bottom) / 2.0f
        }

        canvas.drawText(text, rectF.width() * 0.5f, bounds.top - paint.ascent(), paint)
    }
}
