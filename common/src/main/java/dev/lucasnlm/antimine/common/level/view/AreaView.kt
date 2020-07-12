package dev.lucasnlm.antimine.common.level.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.AreaPaintSettings
import dev.lucasnlm.antimine.common.level.models.AreaPalette
import dev.lucasnlm.antimine.common.level.models.Mark

class AreaView : View {
    // Used on Wear OS
    private var isAmbientMode = false
    private var isLowBitAmbient = false

    private var area: Area? = null
    private lateinit var paintSettings: AreaPaintSettings
    private lateinit var palette: AreaPalette

    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(context, GestureDetector.SimpleOnGestureListener())
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        isHapticFeedbackEnabled = true
    }

    fun setOnDoubleClickListener(listener: GestureDetector.OnDoubleTapListener) {
        gestureDetector.setOnDoubleTapListener(listener)
    }

    fun bindField(area: Area, isAmbientMode: Boolean, isLowBitAmbient: Boolean, paintSettings: AreaPaintSettings) {
        this.paintSettings = paintSettings

        bindContentDescription(area)

        val changed = arrayOf(
            area != this.area,
            this.isAmbientMode != isAmbientMode,
            this.isLowBitAmbient != isLowBitAmbient
        ).firstOrNull { it } ?: false

        if (changed) {
            this.palette = if (isAmbientMode) {
                AreaPalette.fromContrast(context)
            } else {
                AreaPalette.fromDefault(context)
            }

            this.isAmbientMode = isAmbientMode
            this.isLowBitAmbient = isLowBitAmbient

            this.paintSettings.painter.isAntiAlias = !isAmbientMode || isAmbientMode && !isLowBitAmbient
            this.area = area.copy()

            if (Build.VERSION.SDK_INT >= 23) {
                this.foreground = when {
                    !isAmbientMode && (area.isCovered || area.minesAround > 0) -> getRippleEffect(context)
                    else -> null
                }
            }

            invalidate()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean =
        gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)

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
                area.mark.isNotNone() -> IMPORTANT_FOR_ACCESSIBILITY_YES
                !area.isCovered ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                    } else {
                        IMPORTANT_FOR_ACCESSIBILITY_NO
                    }
                else -> IMPORTANT_FOR_ACCESSIBILITY_YES
            }
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isAmbientMode) {
            setBackgroundResource(android.R.color.black)
        } else {
            setBackgroundResource(android.R.color.transparent)
        }

        area?.run {
            if (enabled) {
                paintOnCanvas(
                    context,
                    canvas,
                    isAmbientMode,
                    isLowBitAmbient,
                    isFocused,
                    paintSettings,
                    palette
                )
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = paintSettings.rectF.width().toInt()
        setMeasuredDimension(size, size)
    }

    private fun getRippleEffect(context: Context): Drawable? {
        val outValue = TypedValue()
        context.theme.resolveAttribute(
            android.R.attr.selectableItemBackground, outValue, true
        )
        return ContextCompat.getDrawable(context, outValue.resourceId)
    }
}
