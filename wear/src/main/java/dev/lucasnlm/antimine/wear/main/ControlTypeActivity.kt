package dev.lucasnlm.antimine.wear.main

import android.os.Bundle
import androidx.wear.widget.WearableLinearLayoutManager
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.wear.R
import dev.lucasnlm.antimine.wear.databinding.ActivityControlTypesBinding
import dev.lucasnlm.antimine.wear.main.models.ControlTypeItem
import dev.lucasnlm.antimine.wear.main.view.ControlTypeListAdapter
import org.koin.android.ext.android.inject

class ControlTypeActivity : ThemedActivity() {
    private lateinit var binding: ActivityControlTypesBinding
    private val preferencesRepository: IPreferencesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityControlTypesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        refreshControlTypeList()

        binding.close.setOnClickListener {
            finish()
        }
    }

    private fun refreshControlTypeList() {
        val currentStyle = preferencesRepository.controlStyle()

        val controlTypes = listOf(
            ControlTypeItem(
                id = 0,
                primaryAction = R.string.open,
                secondaryAction = R.string.flag_tile,
                selected = ControlStyle.Standard == currentStyle,
                onClick = {
                    preferencesRepository.useControlStyle(ControlStyle.Standard)
                    finish()
                },
            ),
            ControlTypeItem(
                id = 1,
                primaryAction = R.string.flag_tile,
                secondaryAction = R.string.open,
                selected = ControlStyle.FastFlag == currentStyle,
                onClick = {
                    preferencesRepository.useControlStyle(ControlStyle.FastFlag)
                    finish()
                },
            ),
            ControlTypeItem(
                id = 2,
                primaryAction = R.string.single_click,
                secondaryAction = R.string.double_click,
                selected = ControlStyle.DoubleClick == currentStyle,
                onClick = {
                    preferencesRepository.useControlStyle(ControlStyle.DoubleClick)
                    finish()
                },
            ),
            ControlTypeItem(
                id = 3,
                primaryAction = R.string.double_click,
                secondaryAction = R.string.single_click,
                selected = ControlStyle.DoubleClickInverted == currentStyle,
                onClick = {
                    preferencesRepository.useControlStyle(ControlStyle.DoubleClickInverted)
                    finish()
                },
            ),
            ControlTypeItem(
                id = 4,
                primaryAction = R.string.switch_control_desc,
                selected = ControlStyle.SwitchMarkOpen == currentStyle,
                onClick = {
                    preferencesRepository.useControlStyle(ControlStyle.SwitchMarkOpen)
                    finish()
                },
            ),
        ).sortedBy {
            !it.selected
        }

        binding.recyclerView.apply {
            setHasFixedSize(true)
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(this@ControlTypeActivity)
            adapter = ControlTypeListAdapter(controlTypes)
        }
    }
}
