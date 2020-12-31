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

class ControlItemView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val radio: AppCompatRadioButton
    private val root: View
    private val firstAction: TextView
    private val firstActionResponse: TextView
    private val secondAction: TextView
    private val secondActionResponse: TextView

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.view_control_item, this, true)

        radio = findViewById(R.id.radio)
        root = findViewById(R.id.root)
        firstAction = findViewById(R.id.firstAction)
        firstActionResponse = findViewById(R.id.firstActionResponse)
        secondAction = findViewById(R.id.secondAction)
        secondActionResponse = findViewById(R.id.secondActionResponse)
    }

    fun bind(controlDetails: ControlDetails) {
        firstAction.text = context.getString(controlDetails.firstActionId)
        firstActionResponse.text = context.getString(controlDetails.firstActionResponseId)
        secondAction.text = context.getString(controlDetails.secondActionId)
        secondActionResponse.text = context.getString(controlDetails.secondActionResponseId)
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
