package dev.lucasnlm.antimine.level.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.GravityCompat
import androidx.core.view.postDelayed
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.lucasnlm.antimine.DeepLink
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.dpToPx
import dev.lucasnlm.antimine.core.isPortrait
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.gdx.GameApplicationListener
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.ext.toInvertedAndroidColor
import dev.lucasnlm.antimine.ui.repository.ThemeRepository
import dev.lucasnlm.external.CrashReporter
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

open class GdxLevelFragment : AndroidFragmentApplication() {
    private val gameViewModel by sharedViewModel<GameViewModel>()
    private val themeRepository: ThemeRepository by inject()
    private val dimensionRepository: IDimensionRepository by inject()
    private val preferencesRepository: IPreferencesRepository by inject()
    private val crashReporter: CrashReporter by inject()

    private val levelApplicationListener by lazy {
        GameApplicationListener(
            context = requireContext(),
            theme = themeRepository.getTheme(),
            preferencesRepository = preferencesRepository,
            dimensionRepository = dimensionRepository,
            forceFreeScroll = preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen,
            crashLogger = {
                crashReporter.sendError(it)
            },
            onSingleTouch = {
                lifecycleScope.launch {
                    gameViewModel.onSingleClick(it.id)
                }
            },
            onLongTouch = {
                lifecycleScope.launch {
                    gameViewModel.onLongClick(it.id)
                }
            }
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
                }
            )

            field.observe(
                viewLifecycleOwner,
                {
                    levelApplicationListener.bindField(it)
                }
            )

            eventObserver.observe(
                viewLifecycleOwner,
                {
                    when (it) {
                        Event.Pause,
                        Event.GameOver,
                        Event.Victory -> levelApplicationListener.setActionsEnabled(false)
                        Event.Running,
                        Event.Resume,
                        Event.ResumeGame,
                        Event.StartNewGame -> {
                            levelApplicationListener.run {
                                setActionsEnabled(true)
                            }
                        }
                        else -> {
                            // Nothing
                        }
                    }
                }
            )
        }

        bindControlSwitcherIfNeeded(view)
    }

    private fun bindControlSwitcherIfNeeded(view: View) {
        view.postDelayed(200L) {
            if (preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen) {
                (view.parent as FrameLayout).apply {
                    val floatingView = FloatingActionButton(context).apply {
                        val palette = themeRepository.getTheme().palette
                        contentDescription = getString(R.string.open)
                        TooltipCompat.setTooltipText(this, getString(R.string.open))
                        gameViewModel.refreshUseOpenOnSwitchControl(true)
                        preferencesRepository.setSwitchControl(true)
                        backgroundTintList = ColorStateList.valueOf(palette.accent.toInvertedAndroidColor(255))
                        setColorFilter(palette.accent.toInvertedAndroidColor(255))
                        setImageResource(R.drawable.touch)

                        compatElevation = 0f
                        alpha = 0f
                        animate().apply {
                            alpha(1.0f)
                            duration = 300L
                            start()
                        }

                        setOnClickListener {
                            if (preferencesRepository.openUsingSwitchControl()) {
                                contentDescription = getString(R.string.flag_tile)
                                TooltipCompat.setTooltipText(this, getString(R.string.switch_control))
                                gameViewModel.refreshUseOpenOnSwitchControl(false)
                                preferencesRepository.setSwitchControl(false)
                                setImageResource(R.drawable.flag_black)
                            } else {
                                contentDescription = getString(R.string.open)
                                TooltipCompat.setTooltipText(this, getString(R.string.open))
                                gameViewModel.refreshUseOpenOnSwitchControl(true)
                                preferencesRepository.setSwitchControl(true)
                                setImageResource(R.drawable.touch)
                            }
                        }
                    }

                    val layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        val padding = context.dpToPx(16)
                        gravity = GravityCompat.END or Gravity.BOTTOM

                        if (context.isPortrait()) {
                            setMargins(0, 0, padding, padding + dimensionRepository.navigationBarHeight())
                        } else {
                            setMargins(0, 0, padding + dimensionRepository.navigationBarHeight(), padding)
                        }
                    }

                    addView(floatingView, layoutParams)
                }
            }
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
