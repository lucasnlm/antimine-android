package dev.lucasnlm.antimine.control

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import dev.lucasnlm.antimine.control.databinding.SwitchButtonBinding
import com.google.android.material.R as GR

class SwitchButtonView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding by lazy {
        val layoutInflater = LayoutInflater.from(context)
        SwitchButtonBinding.inflate(layoutInflater, this, true)
    }

    private var currentSelected: MaterialButton? = null

    private val selectedIconTint =
        MaterialColors.getColorStateListOrNull(
            context,
            GR.attr.colorOnPrimary,
        )
    private val selectedBackgroundTint =
        MaterialColors.getColorStateListOrNull(
            context,
            GR.attr.colorPrimary,
        )
    private val unselectedIconTint = binding.flagButton.iconTint
    private val unselectedBackgroundTint = binding.flagButton.backgroundTintList

    fun setQuestionButtonVisibility(visible: Boolean) {
        binding.questionButton.isVisible = visible
    }

    fun isVertical(): Boolean {
        return binding.buttonLayout.orientation == LinearLayout.VERTICAL
    }

    fun isHorizontal(): Boolean {
        return binding.buttonLayout.orientation == LinearLayout.HORIZONTAL
    }

    fun setHorizontalLayout() {
        binding.buttonLayout.run {
            orientation = LinearLayout.HORIZONTAL
            requestLayout()
        }
    }

    fun setVerticalLayout() {
        binding.buttonLayout.run {
            orientation = LinearLayout.VERTICAL
            requestLayout()
        }
    }

    private fun updateMaterialButtonState(
        target: MaterialButton,
        selected: Boolean,
    ) {
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
        binding.flagButton.isSoundEffectsEnabled = false
        binding.flagButton.setOnClickListener {
            listener?.onClick(it)
            selectFlag()
        }
    }

    fun setOnOpenClickListener(listener: OnClickListener?) {
        binding.openButton.isSoundEffectsEnabled = false
        binding.openButton.setOnClickListener {
            listener?.onClick(it)
            selectOpen()
        }
    }

    fun setOnQuestionClickListener(listener: OnClickListener?) {
        binding.questionButton.isSoundEffectsEnabled = false
        binding.questionButton.setOnClickListener {
            listener?.onClick(it)
            selectQuestionMark()
        }
    }

    fun selectQuestionMark() {
        updateMaterialButtonState(binding.flagButton, false)
        updateMaterialButtonState(binding.questionButton, true)
        updateMaterialButtonState(binding.openButton, false)
    }

    fun selectOpen() {
        updateMaterialButtonState(binding.flagButton, false)
        updateMaterialButtonState(binding.questionButton, false)
        updateMaterialButtonState(binding.openButton, true)
    }

    fun selectFlag() {
        updateMaterialButtonState(binding.flagButton, true)
        updateMaterialButtonState(binding.questionButton, false)
        updateMaterialButtonState(binding.openButton, false)
    }
}
