package dev.lucasnlm.antimine.wear

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnLayout
import androidx.lifecycle.lifecycleScope
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.models.AmbientSettings
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.view.CommonLevelFragment
import dev.lucasnlm.antimine.ui.view.SpaceItemDecoration
import dev.lucasnlm.antimine.common.level.widget.FixedGridLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchLevelFragment : CommonLevelFragment(R.layout.fragment_level) {
    override fun makeNewLayoutManager(boardWidth: Int) =
        FixedGridLayoutManager(false).apply {
            totalColumnCount = boardWidth
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerGrid.doOnLayout {
            lifecycleScope.launch {
                val levelSetup = gameViewModel.loadLastGame()

                withContext(Dispatchers.Main) {
                    recyclerGrid.apply {
                        addItemDecoration(SpaceItemDecoration(R.dimen.field_padding))
                        setHasFixedSize(true)
                        setupRecyclerViewSize(view, levelSetup)
                    }
                }
            }
        }

        gameViewModel.run {
            field.observe(
                viewLifecycleOwner,
                {
                    areaAdapter.bindField(it)
                }
            )

            levelSetup.observe(
                viewLifecycleOwner,
                {
                    getView()?.let { view ->
                        setupRecyclerViewSize(view, it)
                    }
                }
            )

            eventObserver.observe(
                viewLifecycleOwner,
                {
                    if (!gameViewModel.hasPlantedMines()) {
                        recyclerGrid.post {
                            levelSetup.value?.let { minefield ->
                                val size = dimensionRepository.areaSizeWithPadding()
                                val dx = minefield.width * size * 0.25f
                                val dy = minefield.height * size * 0.25f
                                recyclerGrid.smoothScrollBy(dx.toInt(), dy.toInt(), null, 200)
                            }
                        }
                    }

                    when (it) {
                        Event.GameOver,
                        Event.Victory -> areaAdapter.setClickEnabled(false)
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
