package dev.lucasnlm.antimine.common.level.view

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.AppVersionManager
import dev.lucasnlm.antimine.core.dpToPx
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.gdx.GameApplicationListener
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.repository.ThemeRepository
import dev.lucasnlm.external.CrashReporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.system.exitProcess

open class GameRenderFragment : AndroidFragmentApplication() {
    private val gameViewModel by sharedViewModel<GameViewModel>()
    private val themeRepository: ThemeRepository by inject()
    private val dimensionRepository: DimensionRepository by inject()
    private val preferencesRepository: PreferencesRepository by inject()
    private val crashReporter: CrashReporter by inject()
    private val appVersionManager: AppVersionManager by inject()

    private var controlSwitcher: SwitchButtonView? = null
    private val isWatch = appVersionManager.isWatch()

    private val levelApplicationListener by lazy {
        GameApplicationListener(
            context = requireContext(),
            appVersion = appVersionManager,
            themeRepository = themeRepository,
            preferencesRepository = preferencesRepository,
            dimensionRepository = dimensionRepository,
            onSingleTap = {
                lifecycleScope.launch {
                    gameViewModel.onSingleClick(it)
                }
            },
            onDoubleTap = {
                lifecycleScope.launch {
                    gameViewModel.onDoubleClick(it)
                }
            },
            onLongTap = {
                lifecycleScope.launch {
                    gameViewModel.onLongClick(it)
                }
            },
            onEngineReady = {
                lifecycleScope.launch {
                    gameViewModel.sendEvent(GameEvent.EngineReady)
                }
            },
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val config = AndroidApplicationConfiguration().apply {
            numSamples = 0
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
        levelApplicationListener.onPause()
    }

    override fun onResume() {
        super.onResume()
        levelApplicationListener.apply {
            refreshSettings()
            refreshZoom()
        }

        if (preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen && !isWatch) {
            view?.let {
                bindControlSwitcherIfNeeded(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenCreated {
            if (!appVersionManager.isValid()) {
                delay(15 * 1000L)
                exitProcess(0)
            }
        }

        lifecycleScope.launchWhenCreated {
            gameViewModel.observeState().collect {
                levelApplicationListener.bindField(it.field)

                if (it.isActive && !it.isGameCompleted) {
                    levelApplicationListener.setActionsEnabled(true)
                } else {
                    levelApplicationListener.setActionsEnabled(false)
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            gameViewModel.observeState()
                .map { it.seed }
                .distinctUntilChanged()
                .collect {
                    levelApplicationListener.onChangeGame()

                    if (preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen && !isWatch) {
                        bindControlSwitcherIfNeeded(view)
                    }
                }
        }

        lifecycleScope.launchWhenCreated {
            gameViewModel
                .observeState()
                .map { it.minefield }
                .distinctUntilChanged()
                .collect {
                    levelApplicationListener.bindMinefield(it)
                }
        }
    }

    private fun getSwitchControlLayoutParams(): FrameLayout.LayoutParams {
        val context = requireContext()
        return FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            val navHeight = dimensionRepository.navigationBarHeight()
            val baseBottomDp = 48
            val bottomMargin = if (navHeight == 0) {
                context.dpToPx(baseBottomDp)
            } else {
                context.dpToPx(baseBottomDp + 32)
            }
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            setMargins(0, 0, 0, bottomMargin)
        }
    }

    private fun bindControlSwitcherIfNeeded(view: View, delayed: Boolean = true) {
        val controlSwitcher = controlSwitcher
        if (controlSwitcher != null) {
            controlSwitcher.isVisible = preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen
        } else {
            view.postDelayed(if (delayed) 200L else 0L) {
                if (this.controlSwitcher == null) {
                    val isParentFinishing = activity?.isFinishing ?: true
                    if (preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen && !isParentFinishing) {
                        (view.parent as? FrameLayout)?.apply {
                            this@GameRenderFragment.controlSwitcher = SwitchButtonView(context).apply {
                                alpha = 0f
                                animate().apply {
                                    alpha(1.0f)
                                    duration = 300L
                                    start()
                                }

                                isVisible = true
                                layoutParams = getSwitchControlLayoutParams()

                                setQuestionButtonVisibility(preferencesRepository.useQuestionMark())

                                setOnFlagClickListener {
                                    gameViewModel.changeSwitchControlAction(Action.SwitchMark)
                                }

                                setOnOpenClickListener {
                                    gameViewModel.changeSwitchControlAction(Action.OpenTile)
                                }

                                setOnQuestionClickListener {
                                    gameViewModel.changeSwitchControlAction(Action.QuestionMark)
                                }

                                val selectedAction = preferencesRepository.getSwitchControlAction()
                                val openAsDefault =
                                    selectedAction == Action.OpenTile || selectedAction == Action.QuestionMark
                                selectOpenAsDefault(openAsDefault)
                                if (openAsDefault) {
                                    gameViewModel.changeSwitchControlAction(Action.OpenTile)
                                } else {
                                    gameViewModel.changeSwitchControlAction(Action.SwitchMark)
                                }
                            }

                            addView(this@GameRenderFragment.controlSwitcher, getSwitchControlLayoutParams())
                        }
                    }
                }
            }
        }
    }

    companion object {
        val TAG = GameRenderFragment::class.simpleName
    }
}
