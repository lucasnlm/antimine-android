package dev.lucasnlm.antimine.wear.main

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.wear.widget.WearableLinearLayoutManager
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.wear.databinding.ActivityControlTypesBinding
import dev.lucasnlm.antimine.wear.main.models.ControlTypeItem
import dev.lucasnlm.antimine.wear.main.view.ControlTypeListAdapter
import org.koin.android.ext.android.inject
import dev.lucasnlm.antimine.i18n.R as i18n

class ControlTypeActivity : ThemedActivity() {
    private val preferencesRepository: PreferencesRepository by inject()
    private val binding: ActivityControlTypesBinding by lazy {
        ActivityControlTypesBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        refreshControlTypeList()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshControlTypeList() {
        val controlTypes =
            listOf(
                ControlTypeItem(
                    id = 0,
                    primaryAction = i18n.string.single_click,
                    secondaryAction = i18n.string.long_press,
                    controlStyle = ControlStyle.Standard,
                ),
                ControlTypeItem(
                    id = 1,
                    primaryAction = i18n.string.long_press,
                    secondaryAction = i18n.string.single_click,
                    controlStyle = ControlStyle.FastFlag,
                ),
                ControlTypeItem(
                    id = 2,
                    primaryAction = i18n.string.single_click,
                    secondaryAction = i18n.string.double_click,
                    controlStyle = ControlStyle.DoubleClick,
                ),
            )

        binding.recyclerView.apply {
            setHasFixedSize(true)
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(this@ControlTypeActivity)
            adapter =
                ControlTypeListAdapter(
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
