package dev.lucasnlm.antimine.control

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.slider.Slider
import dev.lucasnlm.antimine.control.view.ControlAdapter
import dev.lucasnlm.antimine.control.viewmodel.ControlEvent
import dev.lucasnlm.antimine.control.viewmodel.ControlViewModel
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.ThematicActivity
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.antimine.ui.view.SpaceItemDecoration
import kotlinx.android.synthetic.main.activity_control.*
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject

class ControlActivity : ThematicActivity(R.layout.activity_control), Slider.OnChangeListener {
    private val viewModel: ControlViewModel by inject()
    private val themeRepository: IThemeRepository by inject()
    private val preferencesRepository: IPreferencesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindToolbar()

        val controlAdapter = ControlAdapter(
            themeRepository = themeRepository,
            controls = mutableListOf(),
            selected = preferencesRepository.controlStyle(),
            onControlSelected = { controlStyle ->
                viewModel.sendEvent(ControlEvent.SelectControlStyle(controlStyle))
            }
        )

        recyclerView.apply {
            addItemDecoration(SpaceItemDecoration(R.dimen.theme_divider))
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = controlAdapter
        }

        touchSensibility.addOnChangeListener(this)
        longPress.addOnChangeListener(this)
        doubleClick.addOnChangeListener(this)
        hapticLevel.addOnChangeListener(this)

        toggleButtonTopBar.isChecked = preferencesRepository.showToggleButtonOnTopBar()
        toggleButtonTopBarLabel.setOnClickListener {
            toggleButtonTopBar.isChecked = !toggleButtonTopBar.isChecked
        }
        toggleButtonTopBar.setOnCheckedChangeListener { _, checked ->
            preferencesRepository.setToggleButtonOnTopBar(checked)
        }

        leftHanded.isChecked = preferencesRepository.leftHandedMode()
        leftHandedLabel.setOnClickListener {
            leftHanded.isChecked = !leftHanded.isChecked
        }
        leftHanded.setOnCheckedChangeListener { _, checked ->
            preferencesRepository.setLeftHandedMode(checked)
        }

        lifecycleScope.launchWhenCreated {
            viewModel.observeState().collect {
                controlAdapter.bindControlStyleList(it.selected, it.controls)
                longPress.value = (it.longPress.toFloat() / longPress.stepSize).toInt() * longPress.stepSize
                touchSensibility.value =
                    (it.touchSensibility.toFloat() / touchSensibility.stepSize).toInt() * touchSensibility.stepSize

                val toggleVisible = if (it.showToggleButtonSettings) View.VISIBLE else View.GONE
                toggleButtonTopBar.visibility = toggleVisible
                toggleButtonTopBarLabel.visibility = toggleVisible
                leftHanded.visibility = toggleVisible
                leftHandedLabel.visibility = toggleVisible

                val longPressVisible = when (it.selected) {
                    ControlStyle.Standard, ControlStyle.FastFlag -> View.VISIBLE
                    else -> View.GONE
                }
                longPress.visibility = longPressVisible
                longPressLabel.visibility = longPressVisible

                val doubleClickVisible = when (it.selected) {
                    ControlStyle.DoubleClick, ControlStyle.DoubleClickInverted -> View.VISIBLE
                    else -> View.GONE
                }
                doubleClick.visibility = doubleClickVisible
                doubleClickLabel.visibility = doubleClickVisible

                hapticLevel.value =
                    (it.hapticFeedbackLevel.toFloat() / hapticLevel.stepSize).toInt() * hapticLevel.stepSize
            }
        }
    }

    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        if (fromUser) {
            val progress = value.toInt()
            when (slider) {
                touchSensibility -> {
                    viewModel.sendEvent(ControlEvent.UpdateTouchSensibility(progress))
                }
                longPress -> {
                    viewModel.sendEvent(ControlEvent.UpdateLongPress(progress))
                }
                doubleClick -> {
                    viewModel.sendEvent(ControlEvent.UpdateDoubleClick(progress))
                }
                hapticLevel -> {
                    viewModel.sendEvent(ControlEvent.UpdateHapticFeedbackLevel(progress))
                }
            }
        }
    }

    private fun bindToolbar() {
        section.bind(
            text = R.string.control,
            startButton = R.drawable.back_arrow,
            startDescription = R.string.back,
            startAction = {
                finish()
            },
            endButton = R.drawable.delete,
            endDescription = R.string.delete_all,
            endAction = {
                viewModel.sendEvent(ControlEvent.Reset)
            }
        )
    }
}
