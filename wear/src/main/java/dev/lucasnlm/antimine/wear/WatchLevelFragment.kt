package dev.lucasnlm.antimine.wear

import android.os.Bundle
import android.text.format.DateUtils
import android.view.DisplayCutout
import android.view.View
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.models.AmbientSettings
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.view.CommonLevelFragment
import dev.lucasnlm.antimine.common.level.view.SpaceItemDecoration
import dev.lucasnlm.antimine.core.control.ControlStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class WatchLevelFragment : CommonLevelFragment() {
    override val levelFragmentResId: Int = R.layout.fragment_level

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerGrid = view.findViewById(R.id.recyclerGrid)

        GlobalScope.launch {
            val levelSetup = viewModel.loadLastGame()

            withContext(Dispatchers.Main) {
                recyclerGrid.apply {
                    setHasFixedSize(true)
                    addItemDecoration(SpaceItemDecoration(R.dimen.field_padding))

                    layoutManager = makeNewLayoutManager(levelSetup.width)
                    adapter = areaAdapter
                    alpha = 0.0f

                    animate().apply {
                        alpha(1.0f)
                        duration = DateUtils.SECOND_IN_MILLIS
                    }.start()
                }
            }
        }

        viewModel.run {
            field.observe(
                viewLifecycleOwner,
                Observer {
                    areaAdapter.bindField(it)
                }
            )
            levelSetup.observe(
                viewLifecycleOwner,
                Observer {
                    recyclerGrid.apply {
                        layoutManager = makeNewLayoutManager(it.width)
                    }
                }
            )
            fieldRefresh.observe(
                viewLifecycleOwner,
                Observer {
                    areaAdapter.notifyItemChanged(it)
                }
            )
            eventObserver.observe(
                viewLifecycleOwner,
                Observer {
                    if (it == Event.StartNewGame) {
                        viewModel.useCustomPreferences(false, ControlStyle.FastFlag)
                        recyclerGrid.scrollToPosition(areaAdapter.itemCount / 2)
                    }

                    when (it) {
                        Event.GameOver, Event.Victory -> areaAdapter.setClickEnabled(false)
                        else -> areaAdapter.setClickEnabled(true)
                    }
                }
            )
        }
    }

    fun setAmbientMode(ambientSettings: AmbientSettings) {
        areaAdapter.apply {
            setAmbientMode(ambientSettings.isAmbientMode, ambientSettings.isLowBitAmbient)
            notifyDataSetChanged()
        }

        recyclerGrid.setBackgroundResource(
            if (ambientSettings.isAmbientMode) android.R.color.black else android.R.color.transparent
        )
    }
}
