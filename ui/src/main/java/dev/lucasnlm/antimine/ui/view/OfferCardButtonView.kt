package dev.lucasnlm.antimine.ui.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import dev.lucasnlm.antimine.ui.databinding.ViewOfferCardButtonBinding
import dev.lucasnlm.antimine.ui.ext.ColorExt.toAndroidColor
import dev.lucasnlm.antimine.ui.model.AppTheme

class OfferCardButtonView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding: ViewOfferCardButtonBinding by lazy {
        ViewOfferCardButtonBinding.bind(this)
    }

    init {
        val layoutInflater = LayoutInflater.from(context)
        ViewOfferCardButtonBinding.inflate(layoutInflater, this, true)
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
        val color =
            if (invert || isFocused) {
                theme.palette.background.toAndroidColor()
            } else {
                theme.palette.covered.toAndroidColor()
            }

        val backgroundColor =
            if (invert || isFocused) {
                theme.palette.covered.toAndroidColor()
            } else {
                theme.palette.background.toAndroidColor()
            }

        val label =
            binding.label.apply {
                this.text = text
                if (centralize) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                setTextColor(color)
            }

        val priceView =
            binding.price.apply {
                isVisible = price != null
                if (price != null) {
                    this.text = price
                }

                if (invert) {
                    setTextColor(color)
                }
            }

        val offerView =
            binding.priceOff.apply {
                isVisible = showOffer
                if (invert) {
                    imageTintList = ColorStateList.valueOf(color)
                }
            }

        val iconView =
            binding.icon.apply {
                isVisible = startIcon != null
                if (startIcon != null) {
                    val tintColor = theme.palette.covered.toAndroidColor()
                    imageTintList = ColorStateList.valueOf(tintColor)
                    setImageResource(startIcon)
                }
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
            isSoundEffectsEnabled = false

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
                priceView.setTextColor(inverted)
                iconView.setColorFilter(inverted)
                setCardBackgroundColor(focusedBackgroundColor)
                offerView.imageTintList = ColorStateList.valueOf(inverted)
            }
        }
    }
}
