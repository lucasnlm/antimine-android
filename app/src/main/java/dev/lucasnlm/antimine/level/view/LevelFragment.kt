package dev.lucasnlm.antimine.level.view

import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.core.view.doOnLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import dev.lucasnlm.antimine.DeepLink
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.common.level.view.CommonLevelFragment
import dev.lucasnlm.antimine.common.level.view.SpaceItemDecoration
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
        recyclerGrid = view.findViewById(R.id.recyclerGrid)
        recyclerGrid.doOnLayout {
            lifecycleScope.launch {
                val loadGameUid = checkLoadGameDeepLink()
                val newGameDeepLink = checkNewGameDeepLink()
                val retryDeepLink = checkRetryGameDeepLink()

                val levelSetup = when {
                    loadGameUid != null -> gameViewModel.loadGame(loadGameUid)
                    newGameDeepLink != null -> gameViewModel.startNewGame(newGameDeepLink)
                    retryDeepLink != null -> gameViewModel.retryGame(retryDeepLink)
                    else -> gameViewModel.loadLastGame()
                }

                withContext(Dispatchers.Main) {
                    recyclerGrid.apply {
                        addItemDecoration(SpaceItemDecoration(R.dimen.field_padding))
                        setHasFixedSize(true)
                    }
                    setupRecyclerViewSize(levelSetup)
                }
            }
        }

        gameViewModel.run {
            field.observe(
                viewLifecycleOwner,
                Observer {
                    areaAdapter.bindField(it)
                }
            )

            levelSetup.observe(
                viewLifecycleOwner,
                Observer {
                    setupRecyclerViewSize(it)
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
                    when (it) {
                        Event.Pause,
                        Event.ResumeGameOver,
                        Event.GameOver,
                        Event.Victory,
                        Event.ResumeVictory -> areaAdapter.setClickEnabled(false)
                        Event.Running,
                        Event.Resume,
                        Event.ResumeGame,
                        Event.StartNewGame -> areaAdapter.setClickEnabled(true)
                        null -> { }
                    }
                }
            )
        }
    }

    private fun setupRecyclerViewSize(levelSetup: Minefield) {
        recyclerGrid.apply {
            val horizontalPadding = calcHorizontalPadding(levelSetup.width)
            val verticalPadding = calcVerticalPadding(levelSetup.height)
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
}
