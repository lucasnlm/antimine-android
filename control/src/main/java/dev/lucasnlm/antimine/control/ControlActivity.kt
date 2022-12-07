package dev.lucasnlm.antimine.control

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.slider.Slider
import dev.lucasnlm.antimine.control.view.ControlAdapter
import dev.lucasnlm.antimine.control.viewmodel.ControlEvent
import dev.lucasnlm.antimine.control.viewmodel.ControlViewModel
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.ext.ThematicActivity
import dev.lucasnlm.antimine.ui.model.TopBarAction
import kotlinx.android.synthetic.main.activity_control.*
import org.koin.android.ext.android.inject

class ControlActivity : ThematicActivity(R.layout.activity_control), Slider.OnChangeListener {
    private val viewModel: ControlViewModel by inject()
    private val preferencesRepository: IPreferencesRepository by inject()

    private val toolbar: MaterialToolbar by lazy {
        findViewById(R.id.toolbar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val controlAdapter = ControlAdapter(
            controls = mutableListOf(),
            selected = preferencesRepository.controlStyle(),
            onControlSelected = { controlStyle ->
                viewModel.sendEvent(ControlEvent.SelectControlStyle(controlStyle))
            },
        )

        recyclerView.apply {
            setHasFixedSize(true)
            itemAnimator = null
            layoutManager = LinearLayoutManager(context)
            adapter = controlAdapter
        }

        touchSensibility.addOnChangeListener(this)
        longPress.addOnChangeListener(this)
        doubleClick.addOnChangeListener(this)
        hapticLevel.addOnChangeListener(this)

        lifecycleScope.launchWhenCreated {
            viewModel.observeState().collect {
                controlAdapter.bindControlStyleList(it.selected, it.controls)
                longPress.value = (it.longPress.toFloat() / longPress.stepSize).toInt() * longPress.stepSize
                touchSensibility.value =
                    (it.touchSensibility.toFloat() / touchSensibility.stepSize).toInt() * touchSensibility.stepSize

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
                doubleClick.value = it.doubleClick.toFloat()

                hapticLevel.value =
                    (it.hapticFeedbackLevel.toFloat() / hapticLevel.stepSize).toInt() * hapticLevel.stepSize

                if (it.showReset) {
                    setTopBarAction(
                        TopBarAction(
                            actionName = R.string.delete_all,
                            icon = R.drawable.undo,
                            action = {
                                viewModel.sendEvent(ControlEvent.Reset)
                            },
                        ),
                    )
                } else {
                    setTopBarAction(null)
                }
            }
        }

        bindToolbar(toolbar)
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
}
