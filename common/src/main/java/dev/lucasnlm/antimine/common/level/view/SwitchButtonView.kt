package dev.lucasnlm.antimine.common.level.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import dev.lucasnlm.antimine.common.R

class SwitchButtonView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflate(context, R.layout.switch_button, this)
    }

    private val questionButton: MaterialButton by lazy {
        findViewById(R.id.question)
    }

    private val flagButton: MaterialButton by lazy {
        findViewById(R.id.flag)
    }

    private val openButton: MaterialButton by lazy {
        findViewById(R.id.open)
    }

    private var currentSelected: MaterialButton? = null

    private val selectedIconTint = MaterialColors.getColorStateListOrNull(
        context,
        R.attr.colorOnPrimary,
    )
    private val selectedBackgroundTint = MaterialColors.getColorStateListOrNull(
        context,
        R.attr.colorPrimary,
    )
    private val unselectedIconTint = flagButton.iconTint
    private val unselectedBackgroundTint = flagButton.backgroundTintList

    fun setQuestionButtonVisibility(visible: Boolean) {
        questionButton.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun updateMaterialButtonState(target: MaterialButton, selected: Boolean) {
        target.apply {
            if (selected) {
                currentSelected = target
                iconTint = selectedIconTint
                backgroundTintList = selectedBackgroundTint
            } else {
                iconTint = unselectedIconTint
                backgroundTintList = unselectedBackgroundTint
            }
        }
    }

    fun setOnFlagClickListener(listener: OnClickListener?) {
        flagButton.setOnClickListener {
            listener?.onClick(it)
            updateMaterialButtonState(flagButton, true)
            updateMaterialButtonState(questionButton, false)
            updateMaterialButtonState(openButton, false)
        }
    }

    fun setOnOpenClickListener(listener: OnClickListener?) {
        openButton.setOnClickListener {
            listener?.onClick(it)
            updateMaterialButtonState(flagButton, false)
            updateMaterialButtonState(questionButton, false)
            updateMaterialButtonState(openButton, true)
        }
    }

    fun setOnQuestionClickListener(listener: OnClickListener?) {
        questionButton.setOnClickListener {
            listener?.onClick(it)
            updateMaterialButtonState(flagButton, false)
            updateMaterialButtonState(questionButton, true)
            updateMaterialButtonState(openButton, false)
        }
    }

    fun selectOpenAsDefault(enabled: Boolean) {
        if (currentSelected == null) {
            if (enabled) {
                updateMaterialButtonState(flagButton, false)
                updateMaterialButtonState(questionButton, false)
                updateMaterialButtonState(openButton, true)
            } else {
                updateMaterialButtonState(flagButton, true)
                updateMaterialButtonState(questionButton, false)
                updateMaterialButtonState(openButton, false)
            }
        }
    }
}
