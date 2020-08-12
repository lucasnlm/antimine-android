package dev.lucasnlm.antimine.wear

import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.models.AmbientSettings
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.view.CommonLevelFragment
import dev.lucasnlm.antimine.common.level.view.SpaceItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class WatchLevelFragment : CommonLevelFragment(R.layout.fragment_level) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerGrid = view.findViewById(R.id.recyclerGrid)

        GlobalScope.launch {
            val levelSetup = viewModel.loadLastGame()

            withContext(Dispatchers.Main) {
                recyclerGrid.apply {
                    val horizontalPadding = calcHorizontalPadding(levelSetup.width)
                    val verticalPadding = calcVerticalPadding(levelSetup.height)
                    setHasFixedSize(true)
                    addItemDecoration(SpaceItemDecoration(R.dimen.field_padding))
                    setPadding(horizontalPadding, verticalPadding, 0, 0)
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
                        val horizontalPadding = calcHorizontalPadding(it.width)
                        val verticalPadding = calcVerticalPadding(it.height)
                        layoutManager = makeNewLayoutManager(it.width)
                        setPadding(horizontalPadding, verticalPadding, 0, 0)
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
                        recyclerGrid.scrollToPosition(areaAdapter.itemCount / 2)
                    }

                    when (it) {
                        Event.ResumeGameOver, Event.GameOver,
                        Event.Victory, Event.ResumeVictory -> areaAdapter.setClickEnabled(false)
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
