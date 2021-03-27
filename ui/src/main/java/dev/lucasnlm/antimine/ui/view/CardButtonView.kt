package dev.lucasnlm.antimine.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.material.card.MaterialCardView
import dev.lucasnlm.antimine.ui.R
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.model.AppTheme

class CardButtonView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.view_card_button, this, true)
    }

    fun bind(
        theme: AppTheme,
        invert: Boolean = false,
        @StringRes text: Int,
        @StringRes extra: Int? = null,
        onAction: (View) -> Unit,
        @DrawableRes startIcon: Int? = null,
        @DrawableRes endIcon: Int? = null,
    ) {
        bindView(
            theme = theme,
            invert = invert,
            text = context.getString(text),
            extra = extra?.let { context.getString(it) },
            onAction = onAction,
            startIcon = startIcon,
            endIcon = endIcon
        )
    }

    fun bind(
        theme: AppTheme,
        invert: Boolean = false,
        text: String,
        extra: String? = null,
        onAction: (View) -> Unit,
        @DrawableRes startIcon: Int? = null,
        @DrawableRes endIcon: Int? = null,
    ) {
        bindView(
            theme = theme,
            invert = invert,
            text = text,
            extra = extra,
            onAction = onAction,
            startIcon = startIcon,
            endIcon = endIcon
        )
    }

    fun bindStartIcon(@DrawableRes startIcon: Int) {
        findViewById<ImageView>(R.id.icon).apply {
            visibility = View.VISIBLE
            setImageResource(startIcon)
        }
    }

    private fun bindView(
        theme: AppTheme,
        invert: Boolean = false,
        text: String,
        extra: String? = null,
        onAction: (View) -> Unit,
        @DrawableRes startIcon: Int? = null,
        @DrawableRes endIcon: Int? = null,
    ) {
        val color = if (invert || isFocused) {
            theme.palette.background.toAndroidColor()
        } else {
            theme.palette.covered.toAndroidColor()
        }

        val backgroundColor = if (invert || isFocused) {
            theme.palette.covered.toAndroidColor()
        } else {
            theme.palette.background.toAndroidColor()
        }

        val label = findViewById<TextView>(R.id.label).apply {
            this.text = text
            setTextColor(color)
        }

        val size = findViewById<TextView>(R.id.size).apply {
            if (extra == null) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                this.text = extra
                setTextColor(color)
            }
        }

        val iconView = findViewById<ImageView>(R.id.icon).apply {
            if (startIcon == null) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                setImageResource(startIcon)
            }
            setColorFilter(color)
        }

        val endIconView = findViewById<ImageView>(R.id.endIcon).apply {
            if (endIcon == null) {
                setImageResource(0)
            } else {
                setImageResource(endIcon)
            }
            setColorFilter(color)
        }

        findViewById<MaterialCardView>(R.id.card_view).apply {
            setOnClickListener(onAction)
            strokeColor = if (!invert) {
                color
            } else {
                backgroundColor
            }
            setCardBackgroundColor(backgroundColor)

            setOnFocusChangeListener { _, focused ->
                val focusedBackgroundColor = if (focused) {
                    theme.palette.accent.toAndroidColor()
                } else {
                    theme.palette.background.toAndroidColor()
                }

                val inverted = if (focused) {
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
