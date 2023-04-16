package dev.lucasnlm.antimine.wear.main

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.wear.widget.WearableLinearLayoutManager
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.wear.R
import dev.lucasnlm.antimine.wear.databinding.ActivityControlTypesBinding
import dev.lucasnlm.antimine.wear.main.models.ControlTypeItem
import dev.lucasnlm.antimine.wear.main.view.ControlTypeListAdapter
import org.koin.android.ext.android.inject

class ControlTypeActivity : ThemedActivity() {
    private lateinit var binding: ActivityControlTypesBinding
    private val preferencesRepository: PreferencesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityControlTypesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        refreshControlTypeList()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshControlTypeList() {
        val controlTypes = listOf(
            ControlTypeItem(
                id = 0,
                primaryAction = R.string.single_click,
                secondaryAction = R.string.long_press,
                controlStyle = ControlStyle.Standard,
            ),
            ControlTypeItem(
                id = 1,
                primaryAction = R.string.long_press,
                secondaryAction = R.string.single_click,
                controlStyle = ControlStyle.FastFlag,
            ),
            ControlTypeItem(
                id = 2,
                primaryAction = R.string.single_click,
                secondaryAction = R.string.double_click,
                controlStyle = ControlStyle.DoubleClick,
            ),
            ControlTypeItem(
                id = 4,
                primaryAction = R.string.switch_control_desc,
                controlStyle = ControlStyle.SwitchMarkOpen,
            ),
        )

        binding.recyclerView.apply {
            setHasFixedSize(true)
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(this@ControlTypeActivity)
            adapter = ControlTypeListAdapter(
                controlTypeItemList = controlTypes,
                preferencesRepository = preferencesRepository,
                onChangeControl = {
                    val layoutManager = binding.recyclerView.layoutManager
                    val recyclerViewState = layoutManager?.onSaveInstanceState()
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                    layoutManager?.onRestoreInstanceState(recyclerViewState)
                },
            )
        }
    }
}
