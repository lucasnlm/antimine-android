package dev.lucasnlm.antimine.ui.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.card.MaterialCardView
import dev.lucasnlm.antimine.ui.R
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.model.AppTheme

class OfferCardButtonView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.view_offer_card_button, this, true)
    }

    fun bind(
        theme: AppTheme,
        invert: Boolean = false,
        text: String,
        price: String? = null,
        showOffer: Boolean = false,
        onAction: (View) -> Unit,
        centralize: Boolean = false,
        @DrawableRes startIcon: Int? = null,
    ) {
        bindView(
            theme = theme,
            invert = invert,
            text = text,
            price = price,
            showOffer = showOffer,
            centralize = centralize,
            onAction = onAction,
            startIcon = startIcon,
        )
    }

    private fun bindView(
        theme: AppTheme,
        invert: Boolean = false,
        text: String,
        price: String? = null,
        showOffer: Boolean,
        centralize: Boolean = false,
        onAction: (View) -> Unit,
        @DrawableRes startIcon: Int? = null,
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
            if (centralize) {
                gravity = Gravity.CENTER_HORIZONTAL
            }
            setTextColor(color)
        }

        val priceView = findViewById<TextView>(R.id.price).apply {
            if (price == null) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                this.text = price
            }

            if (invert) {
                setTextColor(color)
            }
        }

        val offerView = findViewById<AppCompatImageView>(R.id.priceOff).apply {
            visibility = if (showOffer) {
                View.VISIBLE
            } else {
                View.GONE
            }

            if (invert) {
                imageTintList = ColorStateList.valueOf(color)
            }
        }

        val iconView = findViewById<ImageView>(R.id.icon).apply {
            if (startIcon == null) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                val tintColor = theme.palette.covered.toAndroidColor()
                imageTintList = ColorStateList.valueOf(tintColor)
                setImageResource(startIcon)
            }
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
                priceView.setTextColor(inverted)
                iconView.setColorFilter(inverted)
                setCardBackgroundColor(focusedBackgroundColor)
                offerView.imageTintList = ColorStateList.valueOf(inverted)
            }
        }
    }
}
