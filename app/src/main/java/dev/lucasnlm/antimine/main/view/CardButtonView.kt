package dev.lucasnlm.antimine.main.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.google.android.material.card.MaterialCardView
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.model.AppTheme

class CardButtonView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        init()
    }

    private fun init() {
        LayoutInflater
            .from(context)
            .inflate(R.layout.view_new_game, this, true)
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
        val color = if (invert) {
            theme.palette.background.toAndroidColor()
        } else {
            theme.palette.accent.toAndroidColor()
        }

        val backgroundColor = if (invert) {
            theme.palette.accent.toAndroidColor()
        } else {
            theme.palette.background.toAndroidColor()
        }

        findViewById<TextView>(R.id.label).apply {
            this.text = text
            setTextColor(color)
        }

        findViewById<TextView>(R.id.size).apply {
            if (extra == null) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                this.text = extra
                setTextColor(color)
            }
        }

        findViewById<ImageView>(R.id.icon).apply {
            if (startIcon == null) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                setImageResource(startIcon)
            }
            setColorFilter(color)
        }

        findViewById<ImageView>(R.id.endIcon).apply {
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
        }
    }
}
