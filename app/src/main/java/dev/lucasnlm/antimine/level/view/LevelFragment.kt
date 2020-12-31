package dev.lucasnlm.antimine.level.view

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnLayout
import androidx.lifecycle.lifecycleScope
import dev.lucasnlm.antimine.DeepLink
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.view.CommonLevelFragment
import dev.lucasnlm.antimine.ui.view.SpaceItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                val loadGameUid = checkLoadGameDeepLink()
                val newGameDeepLink = checkNewGameDeepLink()
                val retryDeepLink = checkRetryGameDeepLink()

                val levelSetup = when {
                    loadGameUid != null -> gameViewModel.loadGame(loadGameUid)
                    newGameDeepLink != null -> gameViewModel.startNewGame(newGameDeepLink)
                    retryDeepLink != null -> gameViewModel.retryGame(retryDeepLink)
                    else -> gameViewModel.loadGame()
                }

                withContext(Dispatchers.Main) {
                    recyclerGrid.apply {
                        addItemDecoration(SpaceItemDecoration(R.dimen.field_padding))
                        setHasFixedSize(true)
                    }
                    setupRecyclerViewSize(view, levelSetup)
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
                { minefield ->
                    getView()?.doOnLayout { view ->
                        setupRecyclerViewSize(view, minefield)
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
                                val actionBarSize = dimensionRepository.actionBarSize()
                                val dx = minefield.width * size * 0.25f
                                val dy = (minefield.height * size + actionBarSize) * 0.25f - size * 3

                                recyclerGrid.smoothScrollBy(dx.toInt(), dy.toInt(), null, 200)
                            }
                        }
                    }

                    when (it) {
                        Event.Pause,
                        Event.GameOver,
                        Event.Victory -> areaAdapter.setClickEnabled(false)
                        Event.Running,
                        Event.Resume,
                        Event.ResumeGame,
                        Event.StartNewGame -> areaAdapter.setClickEnabled(true)
                        else -> { }
                    }
                }
            )
        }
    }

    private fun checkNewGameDeepLink(): Difficulty? = activity?.intent?.data?.let { uri ->
        if (uri.scheme == DeepLink.SCHEME && uri.authority == DeepLink.NEW_GAME_AUTHORITY) {
            when (uri.pathSegments.firstOrNull()) {
                DeepLink.BEGINNER_PATH -> Difficulty.Beginner
                DeepLink.INTERMEDIATE_PATH -> Difficulty.Intermediate
                DeepLink.EXPERT_PATH -> Difficulty.Expert
                DeepLink.STANDARD_PATH -> Difficulty.Standard
                DeepLink.CUSTOM_PATH -> Difficulty.Custom
                else -> null
            }
        } else {
            null
        }
    }

    private fun checkLoadGameDeepLink(): Int? = activity?.intent?.data?.let { uri ->
        if (uri.scheme == DeepLink.SCHEME && uri.authority == DeepLink.LOAD_GAME_AUTHORITY) {
            uri.pathSegments.firstOrNull()?.toIntOrNull()
        } else {
            null
        }
    }

    private fun checkRetryGameDeepLink(): Int? = activity?.intent?.data?.let { uri ->
        if (uri.scheme == DeepLink.SCHEME && uri.authority == DeepLink.RETRY_HOST_AUTHORITY) {
            uri.pathSegments.firstOrNull()?.toIntOrNull()
        } else {
            null
        }
    }

    companion object {
        val TAG = LevelFragment::class.simpleName
    }
}
