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

    fun bindText(@StringRes text: Int) {
        findViewById<TextView>(R.id.text).apply {
            this.text = context.getString(text)
        }
    }

    fun showEndAction(visible: Boolean) {
        findViewById<ImageView>(R.id.endButton).visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun bind(
        text: String,
        @DrawableRes startButton: Int? = null,
        startAction: ((View) -> Unit)? = null,
        startDescription: String? = null,
        @DrawableRes endButton: Int? = null,
        endAction: ((View) -> Unit)? = null,
        endDescription: String? = null,
    ) {
        bindView(
            text = text,
            startButton = startButton,
            startAction = startAction,
            startDescription = startDescription,
            endButton = endButton,
            endAction = endAction,
            endDescription = endDescription,
        )
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
        bindView(
            text = context.getString(text),
            startButton = startButton,
            startAction = startAction,
            startDescription = startDescription?.let { context.getString(it) },
            endButton = endButton,
            endAction = endAction,
            endDescription = endDescription?.let { context.getString(it) },
        )
    }

    private fun bindView(
        text: String,
        @DrawableRes startButton: Int? = null,
        startAction: ((View) -> Unit)? = null,
        startDescription: String? = null,
        @DrawableRes endButton: Int? = null,
        endAction: ((View) -> Unit)? = null,
        endDescription: String? = null,
    ) {
        findViewById<TextView>(R.id.text).apply {
            this.text = text
        }

        startButton?.let { button ->
            findViewById<ImageView>(R.id.startButton).apply {
                visibility = View.VISIBLE
                setImageResource(button)
                startDescription?.let { label ->
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
                endDescription?.let { label ->
                    contentDescription = label
                    TooltipCompat.setTooltipText(this, label)
                }
                importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
                setOnClickListener(endAction)
            }
        }
    }
}
