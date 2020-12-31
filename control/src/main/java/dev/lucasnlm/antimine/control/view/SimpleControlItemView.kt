package dev.lucasnlm.antimine.control.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatRadioButton
import dev.lucasnlm.antimine.control.R
import dev.lucasnlm.antimine.control.models.ControlDetails

class SimpleControlItemView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val radio: AppCompatRadioButton
    private val root: View
    private val firstAction: TextView

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.view_control_item_simple, this, true)

        radio = findViewById(R.id.radio)
        root = findViewById(R.id.root)
        firstAction = findViewById(R.id.firstAction)
    }

    fun bind(controlDetails: ControlDetails) {
        firstAction.text = context.getString(controlDetails.firstActionId)
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
