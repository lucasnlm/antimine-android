package dev.lucasnlm.antimine.control

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dev.lucasnlm.antimine.control.view.ControlAdapter
import dev.lucasnlm.antimine.control.viewmodel.ControlEvent
import dev.lucasnlm.antimine.control.viewmodel.ControlViewModel
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.ui.ThematicActivity
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.antimine.ui.view.SpaceItemDecoration
import kotlinx.android.synthetic.main.activity_control.*
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject

class ControlActivity : ThematicActivity(R.layout.activity_control), SeekBar.OnSeekBarChangeListener {
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

        touchSensibility.setOnSeekBarChangeListener(this)
        longPress.setOnSeekBarChangeListener(this)
        doubleClick.setOnSeekBarChangeListener(this)

        toggleButtonTopBar.isChecked = preferencesRepository.showToggleButtonOnTopBar()
        toggleButtonTopBarLabel.setOnClickListener {
            toggleButtonTopBar.isChecked = !toggleButtonTopBar.isChecked
        }
        toggleButtonTopBar.setOnCheckedChangeListener { _, checked ->
            preferencesRepository.setToggleButtonOnTopBar(checked)
        }

        lifecycleScope.launchWhenCreated {
            viewModel.observeState().collect {
                controlAdapter.bindControlStyleList(it.selected, it.controls)
                longPress.progress = it.longPress
                touchSensibility.progress = it.touchSensibility

                val toggleVisible = if (it.showToggleButtonSettings) View.VISIBLE else View.GONE
                toggleButtonTopBar.visibility = toggleVisible
                toggleButtonTopBarLabel.visibility = toggleVisible
            }
        }
    }

    override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            when (seekbar) {
                touchSensibility -> {
                    viewModel.sendEvent(ControlEvent.UpdateTouchSensibility(progress))
                }
                longPress -> {
                    viewModel.sendEvent(ControlEvent.UpdateLongPress(progress))
                }
                doubleClick -> {
                    viewModel.sendEvent(ControlEvent.UpdateDoubleClick(progress))
                }
            }
        }
    }

    override fun onStartTrackingTouch(seekbar: SeekBar?) {
        // Empty
    }

    override fun onStopTrackingTouch(seekbar: SeekBar?) {
        // Empty
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
