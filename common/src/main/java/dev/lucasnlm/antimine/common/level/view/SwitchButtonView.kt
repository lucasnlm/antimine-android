package dev.lucasnlm.antimine.common.level.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.TooltipCompat
import com.google.android.material.card.MaterialCardView
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.core.dpToPx
import dev.lucasnlm.antimine.gdx.toOppositeMax
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.model.AreaPalette

class SwitchButtonView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val card: MaterialCardView
    private val icon: ImageView

    private val defaultElevation = 2f
    private val defaultRadius = 8
    private val defaultStroke = 1

    init {
        inflate(context, R.layout.switch_button, this)
        card = findViewById(R.id.card)
        icon = findViewById(R.id.icon)
    }

    fun bindAsOpenAction(palette: AreaPalette) {
        val openLabel = context.getString(R.string.open)
        TooltipCompat.setTooltipText(card, openLabel)
        card.apply {
            contentDescription = openLabel
            backgroundTintList = ColorStateList.valueOf(palette.accent.toAndroidColor(255))
            radius = context.dpToPx(defaultRadius).toFloat()
            elevation = defaultElevation

            if (palette.accent == palette.covered) {
                val strokeColor = ColorStateList.valueOf(palette.background.toAndroidColor(255))
                setStrokeColor(strokeColor)
                strokeWidth = context.dpToPx(defaultStroke)
            }
        }

        val iconColor = palette.covered.toOppositeMax(0.9f)
        icon.apply {
            setImageResource(R.drawable.touch)
            imageTintList = ColorStateList.valueOf(iconColor.toIntBits())
        }
    }

    fun bindAsFlagAction(palette: AreaPalette) {
        val openLabel = context.getString(R.string.flag_tile)
        TooltipCompat.setTooltipText(card, openLabel)
        card.apply {
            contentDescription = openLabel
            backgroundTintList = ColorStateList.valueOf(palette.accent.toAndroidColor(255))
            radius = context.dpToPx(defaultRadius).toFloat()
            elevation = defaultElevation

            if (palette.accent == palette.covered) {
                val strokeColor = ColorStateList.valueOf(palette.background.toAndroidColor(255))
                setStrokeColor(strokeColor)
                strokeWidth = context.dpToPx(defaultStroke)
            }
        }

        val iconColor = palette.covered.toOppositeMax(0.9f)
        icon.apply {
            setImageResource(R.drawable.flag)
            imageTintList = ColorStateList.valueOf(iconColor.toIntBits())
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        card.setOnClickListener(l)
    }
}
