package dev.lucasnlm.antimine.control

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.slider.Slider
import dev.lucasnlm.antimine.control.databinding.ActivityControlBinding
import dev.lucasnlm.antimine.control.view.ControlAdapter
import dev.lucasnlm.antimine.control.viewmodel.ControlEvent
import dev.lucasnlm.antimine.control.viewmodel.ControlViewModel
import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.ui.model.TopBarAction
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import dev.lucasnlm.antimine.i18n.R as i18n

class ControlActivity : ThemedActivity(), Slider.OnChangeListener {
    private val binding: ActivityControlBinding by lazy {
        ActivityControlBinding.inflate(layoutInflater)
    }

    private val viewModel: ControlViewModel by inject()
    private val preferencesRepository: PreferencesRepository by inject()
    private val gameAudioManager: GameAudioManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val controlAdapter =
            ControlAdapter(
                controls = mutableListOf(),
                selected = preferencesRepository.controlStyle(),
                onControlSelected = { controlStyle ->
                    viewModel.sendEvent(ControlEvent.SelectControlStyle(controlStyle))
                    gameAudioManager.playClickSound()
                },
            )

        binding.recyclerView.apply {
            setHasFixedSize(true)
            itemAnimator = null
            layoutManager = LinearLayoutManager(context)
            adapter = controlAdapter
        }

        binding.touchSensibility.addOnChangeListener(this)
        binding.longPress.addOnChangeListener(this)
        binding.doubleClick.addOnChangeListener(this)
        binding.hapticLevel.addOnChangeListener(this)

        when (preferencesRepository.defaultSwitchButton()) {
            Action.SwitchMark -> {
                binding.switchButtonView.selectFlag()
            }
            else -> {
                binding.switchButtonView.selectOpen()
            }
        }

        binding.switchButtonView.setOnOpenClickListener {
            preferencesRepository.setDefaultSwitchButton(Action.OpenTile)
        }

        binding.switchButtonView.setOnFlagClickListener {
            preferencesRepository.setDefaultSwitchButton(Action.SwitchMark)
        }

        lifecycleScope.launch {
            viewModel.observeState().collect {
                controlAdapter.bindControlStyleList(it.selected, it.controls)
                val longPress: Slider = binding.longPress
                val touchSensibility: Slider = binding.touchSensibility
                val hapticLevel: Slider = binding.hapticLevel

                longPress.value = (it.longPress.toFloat() / longPress.stepSize).toInt() * longPress.stepSize
                touchSensibility.value =
                    (it.touchSensibility.toFloat() / touchSensibility.stepSize).toInt() * touchSensibility.stepSize

                val longPressVisible =
                    when (it.selected) {
                        ControlStyle.Standard, ControlStyle.FastFlag -> true
                        else -> false
                    }
                longPress.isVisible = longPressVisible
                binding.longPressLabel.isVisible = longPressVisible

                val doubleClickVisible =
                    when (it.selected) {
                        ControlStyle.DoubleClick, ControlStyle.DoubleClickInverted -> true
                        else -> false
                    }
                binding.doubleClick.isVisible = doubleClickVisible
                binding.doubleClickLabel.isVisible = doubleClickVisible
                binding.doubleClick.value = it.doubleClick.toFloat()

                hapticLevel.value =
                    (it.hapticFeedbackLevel.toFloat() / hapticLevel.stepSize).toInt() * hapticLevel.stepSize

                if (it.showReset) {
                    setTopBarAction(
                        TopBarAction(
                            name = i18n.string.delete_all,
                            icon = R.drawable.undo,
                            action = {
                                viewModel.sendEvent(ControlEvent.Reset)
                            },
                        ),
                    )
                } else {
                    setTopBarAction(null)
                }

                binding.controlDefault.isVisible = it.selected == ControlStyle.SwitchMarkOpen
            }
        }

        bindToolbar(binding.toolbar)
    }

    override fun onValueChange(
        slider: Slider,
        value: Float,
        fromUser: Boolean,
    ) {
        if (fromUser) {
            val progress = value.toInt()
            when (slider) {
                binding.touchSensibility -> {
                    viewModel.sendEvent(ControlEvent.UpdateTouchSensibility(progress))
                }
                binding.longPress -> {
                    viewModel.sendEvent(ControlEvent.UpdateLongPress(progress))
                }
                binding.doubleClick -> {
                    viewModel.sendEvent(ControlEvent.UpdateDoubleClick(progress))
                }
                binding.hapticLevel -> {
                    viewModel.sendEvent(ControlEvent.UpdateHapticFeedbackLevel(progress))
                }
            }
        }
    }
}
