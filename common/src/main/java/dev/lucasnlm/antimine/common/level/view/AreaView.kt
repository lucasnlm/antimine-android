package dev.lucasnlm.antimine.common.level.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import dev.lucasnlm.antimine.common.level.data.Area
import android.graphics.RectF
import dev.lucasnlm.antimine.common.level.repository.DrawableRepository
import android.util.TypedValue
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.view.ViewCompat
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.data.Mark

class AreaView : View {
    private var covered = true
    private var minesAround: Int = -1
    private var mark = Mark.None
    private var hasMine = false
    private var mistake = false
    private var isAmbientMode = false
    private var isLowBitAmbient = false
    private var highlighted = false

    private lateinit var paintSettings: AreaPaintSettings
    private val drawableRepository = DrawableRepository()

    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    fun bindField(area: Area, isAmbientMode: Boolean, isLowBitAmbient: Boolean, paintSettings: AreaPaintSettings) {
        this.paintSettings = paintSettings
        minesAround = if (covered) -1 else area.minesAround

        bindContentDescription(area)

        val changed = arrayOf(
            this.isAmbientMode != isAmbientMode,
            covered != area.isCovered,
            minesAround != area.minesAround,
            mark != area.mark,
            hasMine != area.hasMine,
            mistake != area.mistake,
            minesAround != area.minesAround,
            highlighted != area.highlighted
        ).find { it } ?: false

        // Used on Wear OS
        this.isAmbientMode = isAmbientMode
        this.isLowBitAmbient = isLowBitAmbient

        paintSettings.painter.isAntiAlias = !isAmbientMode || isAmbientMode && !isLowBitAmbient

        covered = area.isCovered
        mark = area.mark
        hasMine = area.hasMine
        mistake = area.mistake
        minesAround = area.minesAround
        highlighted = area.highlighted
        if (Build.VERSION.SDK_INT >= 23) {
            this.foreground = when {
                !isAmbientMode && covered -> getRippleEffect(context)
                else -> null
            }
        }

        if (changed) {
            invalidate()
        }
    }

    @SuppressLint("InlinedApi")
    private fun bindContentDescription(area: Area) {
        contentDescription = when {
            area.mark == Mark.Flag -> {
                context.getString(if (area.mistake) R.string.desc_wrongly_marked_area else R.string.desc_marked_area)
            }
            area.mark == Mark.Question -> context.getString(R.string.desc_marked_area)
            area.isCovered -> context.getString(R.string.desc_convered_area)
            !area.isCovered && area.minesAround > 0 -> area.minesAround.toString()
            !area.isCovered && area.hasMine -> context.getString(R.string.exploded_mine)
            else -> ""
        }

        ViewCompat.setImportantForAccessibility(
            this,
            when {
                area.minesAround != 0 -> IMPORTANT_FOR_ACCESSIBILITY_YES
                area.hasMine -> IMPORTANT_FOR_ACCESSIBILITY_YES
                area.mistake -> IMPORTANT_FOR_ACCESSIBILITY_YES
                area.mark != Mark.None && area.mark != Mark.PurposefulNone -> IMPORTANT_FOR_ACCESSIBILITY_YES
                !area.isCovered -> IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                else -> IMPORTANT_FOR_ACCESSIBILITY_YES
            }
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paintSettings.run {
            if (isAmbientMode) {
                setBackgroundResource(android.R.color.black)
            } else {
                setBackgroundResource(android.R.color.transparent)
            }

            if (covered) {
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
                        val padding = resources.getDimension(R.dimen.mark_padding).toInt()

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
                    val mine = when {
                        isAmbientMode -> drawableRepository.provideMineLow(context)
                        mistake -> drawableRepository.provideMineExploded(context)
                        else -> drawableRepository.provideMine(context)
                    }

                    mine?.setBounds(
                        rectF.left.toInt(),
                        rectF.top.toInt(),
                        rectF.right.toInt(),
                        rectF.bottom.toInt()
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
                    drawText(canvas, painter, minesAround.toString())
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

    private fun getRippleEffect(context: Context): Drawable? {
        val outValue = TypedValue()
        context.theme.resolveAttribute(
            android.R.attr.selectableItemBackground, outValue, true
        )
        return ContextCompat.getDrawable(context, outValue.resourceId)
    }

    private fun drawText(canvas: Canvas, paint: Paint, text: String) {
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
}
