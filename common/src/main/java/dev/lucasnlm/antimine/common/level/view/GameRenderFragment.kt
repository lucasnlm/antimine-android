package dev.lucasnlm.antimine.common.level.view

import android.content.res.Resources.getSystem
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.postDelayed
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.IAppVersionManager
import dev.lucasnlm.antimine.core.dpToPx
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.gdx.GameApplicationListener
import dev.lucasnlm.antimine.gdx.models.RenderQuality
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.external.ICrashReporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.system.exitProcess

open class GameRenderFragment : AndroidFragmentApplication() {
    private val gameViewModel by sharedViewModel<GameViewModel>()
    private val themeRepository: IThemeRepository by inject()
    private val dimensionRepository: IDimensionRepository by inject()
    private val preferencesRepository: IPreferencesRepository by inject()
    private val crashReporter: ICrashReporter by inject()
    private val appVersionManager: IAppVersionManager by inject()

    private var controlSwitcher: SwitchButtonView? = null

    private val levelApplicationListener by lazy {
        GameApplicationListener(
            context = requireContext(),
            appVersion = appVersionManager,
            themeRepository = themeRepository,
            preferencesRepository = preferencesRepository,
            dimensionRepository = dimensionRepository,
            crashLogger = {
                crashReporter.sendError(it)
            },
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
            quality = getQuality(),
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

        if (preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen) {
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

                if (it.turn == 0) {
                    bindControlSwitcherIfNeeded(view, false)
                }

                bindControlSwitcherIfNeeded(view, false)

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

                    if (preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen) {
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

    private fun getQuality(): RenderQuality {
        val width = getSystem().displayMetrics.widthPixels
        return when {
            width < 900 -> {
                RenderQuality.Low
            }
            width < 1080 -> {
                RenderQuality.Mid
            }
            else -> {
                RenderQuality.High
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
            if (preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen) {
                controlSwitcher.apply {
                    visibility = View.VISIBLE
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
                    val openAsDefault = selectedAction == Action.OpenTile || selectedAction == Action.QuestionMark
                    selectOpenAsDefault(openAsDefault)

                    if (selectedAction == Action.QuestionMark) {
                        preferencesRepository.setSwitchControl(Action.OpenTile)
                    }
                }
            } else {
                controlSwitcher.visibility = View.GONE
            }
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
