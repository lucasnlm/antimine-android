package dev.lucasnlm.antimine.level.view

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.isNotEmpty
import androidx.lifecycle.lifecycleScope
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.view.CommonLevelFragment
import dev.lucasnlm.antimine.core.isAndroidTv
import dev.lucasnlm.antimine.preferences.models.Minefield
import kotlinx.coroutines.launch

open class LevelFragment : CommonLevelFragment(R.layout.fragment_level) {
    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            gameViewModel.saveGame()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerGrid.doOnLayout {
            lifecycleScope.launch {
                gameViewModel.loadGame()
            }
        }

        gameViewModel.run {
            field.observe(
                viewLifecycleOwner,
                {
                    areaAdapter.bindField(it)
                    focusOnCenterIfNeeded()
                }
            )

            levelSetup.observe(
                viewLifecycleOwner,
                { minefield ->
                    getView()?.doOnLayout { view ->
                        setupRecyclerViewSize(view, minefield)
                    }
                }
            )

            eventObserver.observe(
                viewLifecycleOwner,
                {
                    if (!gameViewModel.hasPlantedMines() && activity?.isFinishing == false) {
                        levelSetup.value?.let(::centerMinefield)
                    }

                    when (it) {
                        Event.Pause,
                        Event.GameOver,
                        Event.Victory -> areaAdapter.setClickEnabled(false)
                        Event.Running,
                        Event.Resume,
                        Event.ResumeGame,
                        Event.StartNewGame -> areaAdapter.setClickEnabled(true)
                        else -> {
                        }
                    }
                }
            )
        }
    }

    private fun centerMinefield(minefield: Minefield) = with(recyclerGrid) {
        post {
            activity?.let {
                val singleAreaSize = dimensionRepository.areaSizeWithPadding()
                val actionBarSize = dimensionRepository.actionBarSizeWithStatus()
                val displayMetrics = DisplayMetrics()
                it.windowManager.defaultDisplay.getRealMetrics(displayMetrics)
                val screenHeight = if (context.isAndroidTv()) {
                    displayMetrics.heightPixels
                } else {
                    displayMetrics.heightPixels - actionBarSize
                }

                val screenWidth = displayMetrics.widthPixels
                val boardWidth = singleAreaSize * minefield.width
                val boardHeight = singleAreaSize * minefield.height

                val multiplierY = if (boardHeight > screenHeight) {
                    (boardHeight / screenHeight - 1) * 0.5
                } else {
                    0.0
                }

                val multiplierX = if (boardWidth > screenWidth) {
                    (boardWidth / screenWidth - 1) * 0.5
                } else {
                    0.0
                }

                val dx = (boardWidth - screenWidth).coerceAtLeast(0.0f) * multiplierX
                val dy = (boardHeight - screenHeight).coerceAtLeast(0.0f) * multiplierY

                smoothScrollBy(dx.toInt(), dy.toInt(), null, 300)
                post {
                    requestLayout()
                    focusOnCenterIfNeeded()
                }
            }
        }
    }

    private fun focusOnCenterIfNeeded() {
        if (context?.isAndroidTv() == true) {
            view?.post {
                gameViewModel.levelSetup.value?.let { minefield ->
                    recyclerGrid.let {
                        if (!gameViewModel.hasPlantedMines() && it.isNotEmpty()) {
                            val index = minefield.width * (minefield.height / 2) + (minefield.width / 2)
                            it.getChildAt(index)?.requestFocus()
                        }
                    }
                }
            }
        }
    }

    companion object {
        val TAG = LevelFragment::class.simpleName
    }
}
