package dev.lucasnlm.antimine.level.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import dev.lucasnlm.antimine.DeepLink
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.gdx.LevelApplicationListener
import dev.lucasnlm.antimine.ui.repository.ThemeRepository
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

open class GdxLevelFragment : AndroidFragmentApplication() {
    protected val gameViewModel by sharedViewModel<GameViewModel>()
    private val themeRepository: ThemeRepository by inject()
    private val dimensionRepository: IDimensionRepository by inject()

    private val levelApplicationListener by lazy {
        LevelApplicationListener(
            context = requireContext(),
            theme = themeRepository.getTheme(),
            dimensionRepository = dimensionRepository
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val config = AndroidApplicationConfiguration().apply {
            numSamples = 2
            useAccelerometer = false
            useCompass = false
            useGyroscope = false
            useWakelock = false
        }
        return initializeForView(levelApplicationListener, config)
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            gameViewModel.saveGame()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            val loadGameUid = checkLoadGameDeepLink()
            val newGameDeepLink = checkNewGameDeepLink()
            val retryDeepLink = checkRetryGameDeepLink()

            when {
                loadGameUid != null -> gameViewModel.loadGame(loadGameUid)
                newGameDeepLink != null -> gameViewModel.startNewGame(newGameDeepLink)
                retryDeepLink != null -> gameViewModel.retryGame(retryDeepLink)
                else -> gameViewModel.loadGame()
            }
        }

        gameViewModel.run {
            levelSetup.observe(
                viewLifecycleOwner,
                { minefield ->
                    levelApplicationListener.bindMinefield(minefield)
                    // focusOnCenterIfNeeded()
                }
            )

            field.observe(
                viewLifecycleOwner,
                {
                    levelApplicationListener.bindField(it)
                    //focusOnCenterIfNeeded()
                }
            )

            eventObserver.observe(
                viewLifecycleOwner,
                {
                    //if (!gameViewModel.hasPlantedMines() && activity?.isFinishing == false) {
                    //    levelSetup.value?.let(::centerMinefield)
                    //}

                    when (it) {
                        Event.Pause,
                        Event.GameOver,
                        Event.Victory -> levelApplicationListener.setActionsEnabled(false)
                        Event.Running,
                        Event.Resume,
                        Event.ResumeGame,
                        Event.StartNewGame -> levelApplicationListener.setActionsEnabled(true)
                        else -> {
                            // Nothing
                        }
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
        val TAG = GdxLevelFragment::class.simpleName
    }
}
