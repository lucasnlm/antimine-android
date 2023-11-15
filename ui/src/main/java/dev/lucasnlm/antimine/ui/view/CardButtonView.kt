package dev.lucasnlm.antimine.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.google.android.material.color.MaterialColors
import dev.lucasnlm.antimine.ui.databinding.ViewCardButtonBinding
import dev.lucasnlm.antimine.ui.ext.ColorExt.toAndroidColor
import dev.lucasnlm.antimine.ui.model.AppTheme
import com.google.android.material.R as GR

class CardButtonView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding: ViewCardButtonBinding by lazy {
        ViewCardButtonBinding.bind(this)
    }

    init {
        val layoutInflater = LayoutInflater.from(context)
        ViewCardButtonBinding.inflate(layoutInflater, this, true)
    }

    fun bind(
        theme: AppTheme,
        invert: Boolean = false,
        @StringRes text: Int,
        @StringRes extra: Int? = null,
        onAction: (View) -> Unit,
        centralize: Boolean = false,
        @DrawableRes startIcon: Int? = null,
        @DrawableRes endIcon: Int? = null,
    ) {
        bindView(
            theme = theme,
            invert = invert,
            text = context.getString(text),
            extra = extra?.let { context.getString(it) },
            onAction = onAction,
            centralize = centralize,
            startIcon = startIcon,
            endIcon = endIcon,
        )
    }

    fun bind(
        theme: AppTheme,
        invert: Boolean = false,
        text: String,
        extra: String? = null,
        onAction: (View) -> Unit,
        centralize: Boolean = false,
        @DrawableRes startIcon: Int? = null,
        @DrawableRes endIcon: Int? = null,
    ) {
        bindView(
            theme = theme,
            invert = invert,
            text = text,
            extra = extra,
            centralize = centralize,
            onAction = onAction,
            startIcon = startIcon,
            endIcon = endIcon,
        )
    }

    fun setRadius(radius: Float) {
        binding.cardView.radius = radius
    }

    private fun bindView(
        theme: AppTheme,
        invert: Boolean = false,
        text: String,
        extra: String? = null,
        centralize: Boolean = false,
        onAction: (View) -> Unit,
        @DrawableRes startIcon: Int? = null,
        @DrawableRes endIcon: Int? = null,
    ) {
        val color =
            if (invert || isFocused) {
                com.google.android.material.R.attr.colorSurface
                MaterialColors.getColor(this, GR.attr.colorSurface)
            } else {
                MaterialColors.getColor(this, GR.attr.colorPrimary)
            }

        val backgroundColor =
            if (invert || isFocused) {
                MaterialColors.getColor(this, GR.attr.colorPrimary)
            } else {
                MaterialColors.getColor(this, GR.attr.colorSurface)
            }

        val label =
            binding.label.apply {
                this.text = text
                if (centralize) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                setTextColor(color)
            }

        val size =
            binding.size.apply {
                isVisible = extra != null
                if (extra != null) {
                    this.text = extra
                    setTextColor(color)
                }
            }

        val iconView =
            binding.icon.apply {
                isVisible = startIcon != null
                if (startIcon != null) {
                    setImageResource(startIcon)
                }
                setColorFilter(color)
            }

        val endIconView =
            binding.endIcon.apply {
                if (endIcon == null) {
                    setImageResource(0)
                } else {
                    setImageResource(endIcon)
                }
                setColorFilter(color)
            }

        binding.cardView.apply {
            setOnClickListener(onAction)
            strokeColor =
                if (!invert) {
                    color
                } else {
                    backgroundColor
                }
            setCardBackgroundColor(backgroundColor)

            setOnFocusChangeListener { _, focused ->
                val focusedBackgroundColor =
                    if (focused) {
                        theme.palette.accent.toAndroidColor()
                    } else {
                        theme.palette.background.toAndroidColor()
                    }

                val inverted =
                    if (focused) {
                        theme.palette.background.toAndroidColor()
                    } else {
                        theme.palette.accent.toAndroidColor()
                    }

                label.setTextColor(inverted)
                size.setTextColor(inverted)
                iconView.setColorFilter(inverted)
                endIconView.setColorFilter(inverted)
                setCardBackgroundColor(focusedBackgroundColor)
            }
        }
    }
}
