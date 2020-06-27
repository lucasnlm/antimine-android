package dev.lucasnlm.antimine.control.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatRadioButton
import dev.lucasnlm.antimine.R

class ControlItemView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val radio: AppCompatRadioButton
    private val root: View

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.view_control_item, this, true)

        radio = findViewById(R.id.radio)
        root = findViewById(R.id.root)
    }

    fun setRadio(selected: Boolean) {
        radio.isChecked = selected
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        super.setOnClickListener(listener)
        root.setOnClickListener(listener)
        radio.setOnClickListener(listener)
    }
}
