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
import androidx.appcompat.widget.TooltipCompat
import dev.lucasnlm.antimine.ui.R

class SectionView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.view_section, this, true)
    }

    fun bind(
        @StringRes text: Int,
        @DrawableRes startButton: Int? = null,
        startAction: ((View) -> Unit)? = null,
        @StringRes startDescription: Int? = null,
        @DrawableRes endButton: Int? = null,
        endAction: ((View) -> Unit)? = null,
        @StringRes endDescription: Int? = null,
    ) {
        findViewById<TextView>(R.id.text).apply {
            this.text = context.getString(text)
        }

        startButton?.let { button ->
            findViewById<ImageView>(R.id.startButton).apply {
                visibility = View.VISIBLE
                setImageResource(button)
                startDescription?.let {
                    val label = context.getString(startDescription)
                    contentDescription = label
                    TooltipCompat.setTooltipText(this, label)
                }
                importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
                setOnClickListener(startAction)
            }
        }

        endButton?.let { button ->
            findViewById<ImageView>(R.id.endButton).apply {
                visibility = View.VISIBLE
                setImageResource(button)
                endDescription?.let {
                    val label = context.getString(endDescription)
                    contentDescription = context.getString(endDescription)
                    TooltipCompat.setTooltipText(this, label)
                }
                importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
                setOnClickListener(endAction)
            }
        }
    }
}
