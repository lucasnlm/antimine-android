package dev.lucasnlm.antimine.wear

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnLayout
import androidx.lifecycle.lifecycleScope
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.models.AmbientSettings
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.view.CommonLevelFragment
import dev.lucasnlm.antimine.common.level.view.SpaceItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchLevelFragment : CommonLevelFragment(R.layout.fragment_level) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerGrid = view.findViewById(R.id.recyclerGrid)
        recyclerGrid.doOnLayout {
            lifecycleScope.launch {
                val levelSetup = gameViewModel.loadLastGame()

                withContext(Dispatchers.Main) {
                    recyclerGrid.apply {
                        recyclerGrid.apply {
                            addItemDecoration(SpaceItemDecoration(R.dimen.field_padding))
                            setHasFixedSize(true)
                        }
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
                    if (it == Event.StartNewGame) {
                        recyclerGrid.scrollToPosition(areaAdapter.itemCount / 2)
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
