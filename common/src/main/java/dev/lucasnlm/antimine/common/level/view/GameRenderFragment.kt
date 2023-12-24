package dev.lucasnlm.antimine.common.level.view

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.InputDeviceCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewConfigurationCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import dev.lucasnlm.antimine.common.auto.AutoExt.isAndroidAuto
import dev.lucasnlm.antimine.common.level.logic.VisibleMineStream
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.GameState
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.control.SwitchButtonView
import dev.lucasnlm.antimine.core.AppVersionManager
import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.gdx.GameApplicationListener
import dev.lucasnlm.antimine.gdx.GameContext
import dev.lucasnlm.antimine.gdx.models.GameRenderingContext
import dev.lucasnlm.antimine.gdx.models.InternalPadding
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.repository.ThemeRepository
import dev.lucasnlm.antimine.utils.ContextExt.dpToPx
import dev.lucasnlm.antimine.utils.ContextExt.isPortrait
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
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
    private val appVersionManager: AppVersionManager by inject()
    private val gameAudioManager: GameAudioManager by inject()
    private val visibleMineStream: VisibleMineStream by inject()

    private val layoutParent: FrameLayout? by lazy {
        view?.parent as? FrameLayout
    }

    private var engineReady: Boolean = false
    private val controlSwitcher: SwitchButtonView by lazy { initSwitchButtonView() }
    private val isWatch = appVersionManager.isWatch()
    private val useControlSwitcher = preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen

    private val gameRenderingContext: GameRenderingContext by lazy {
        val context = requireContext()
        GameRenderingContext(
            theme = themeRepository.getTheme(),
            internalPadding = getInternalPadding(),
            areaSize = dimensionRepository.areaSize(),
            navigationBarHeight = dimensionRepository.navigationBarHeight().toFloat(),
            appBarWithStatusHeight = dimensionRepository.actionBarSizeWithStatus().toFloat(),
            appBarHeight = appBarHeight(context),
            joinAreas = themeRepository.getSkin().joinAreas,
            appSkin = themeRepository.getSkin(),
        )
    }

    private val levelApplicationListener by lazy {
        GameApplicationListener(
            gameRenderingContext = gameRenderingContext,
            appVersion = appVersionManager,
            preferencesRepository = preferencesRepository,
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
                if (!engineReady) {
                    onEngineReady()
                }

                lifecycleScope.launch {
                    gameViewModel.sendEvent(GameEvent.EngineReady)
                }
            },
            onActorsLoaded = {
                lifecycleScope.launch {
                    gameViewModel.sendEvent(GameEvent.ActorLoaded)
                }
            },
            onEmptyActors = {
                forceRefresh()
            },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GameContext.refreshColors(themeRepository.getTheme())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val config =
            AndroidApplicationConfiguration().apply {
                numSamples = 2
                useAccelerometer = false
                useCompass = false
                useGyroscope = false
                useWakelock = false
                useImmersiveMode = preferencesRepository.useImmersiveMode()
                disableAudio = true
            }
        return initializeForView(levelApplicationListener, config).apply {
            setOnGenericMotionListener { _, event ->
                if (event.action == MotionEvent.ACTION_SCROLL &&
                    event.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER)
                ) {
                    val delta =
                        -event.getAxisValue(MotionEventCompat.AXIS_SCROLL) *
                            ViewConfigurationCompat.getScaledVerticalScrollFactor(
                                ViewConfiguration.get(context), context,
                            )
                    levelApplicationListener.onScroll(delta)
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        levelApplicationListener.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (engineReady) {
            levelApplicationListener.apply {
                refreshSettings()
                refreshZoom()
            }
        }
    }

    private fun forceRefresh() {
        lifecycleScope.launch {
            gameViewModel.singleState().let(::refreshState)
        }
    }

    private fun onEngineReady() {
        this.engineReady = true

        Gdx.graphics.requestRendering()

        lifecycleScope.launch {
            visibleMineStream
                .observeRequestVisibleMines()
                .map { levelApplicationListener.getVisibleMineActors() }
                .collect(visibleMineStream::update)
        }

        lifecycleScope.launch {
            gameViewModel
                .singleState()
                .let(::refreshState)
        }

        lifecycleScope.launch {
            gameViewModel
                .observeState()
                .distinctUntilChangedBy { it.field }
                .collect(::refreshState)
        }

        lifecycleScope.launch {
            gameViewModel
                .observeState()
                .distinctUntilChangedBy { it.isActive }
                .collect { state ->
                    val areActionsEnabled = state.isActive
                    levelApplicationListener.setActionsEnabled(areActionsEnabled)
                    Gdx.graphics.requestRendering()
                    syncControlSwitcher(state.selectedAction)
                }
        }

        lifecycleScope.launch {
            gameViewModel.observeState()
                .map { it.seed to it.minefield }
                .distinctUntilChanged()
                .collect { (_, minefield) ->
                    levelApplicationListener.onChangeGame()
                    levelApplicationListener.bindMinefield(minefield)
                }
        }
    }

    private fun refreshState(state: GameState) {
        levelApplicationListener.bindField(state.field)

        val areActionsEnabled = state.isActive
        levelApplicationListener.setActionsEnabled(areActionsEnabled)

        syncControlSwitcher(state.selectedAction)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            if (!appVersionManager.isValid()) {
                delay(MAX_INVALID_TIME_S * DateUtils.SECOND_IN_MILLIS)
                exitProcess(0)
            }
        }

        lifecycleScope.launch {
            gameViewModel.observeState()
                .map { it.seed }
                .distinctUntilChanged()
                .collect {
                    if (preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen && !isWatch) {
                        bindControlSwitcherIfNeeded(view)
                    }
                }
        }

        if (savedInstanceState != null && engineReady) {
            onEngineReady()
        }
    }

    private fun getSwitchControlLayoutParams(): FrameLayout.LayoutParams {
        val context = requireContext()
        val isPortrait = context.isPortrait()
        return FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            if (isPortrait) {
                val navHeight = dimensionRepository.navigationBarHeight()
                val bottomMargin =
                    if (navHeight == 0) {
                        context.dpToPx(BOTTOM_MARGIN_WITHOUT_NAV_DP)
                    } else {
                        context.dpToPx(BOTTOM_MARGIN_WITH_NAV_DP)
                    }
                val autoBottomMargin =
                    if (context.isAndroidAuto()) {
                        bottomMargin * 1.5
                    } else {
                        bottomMargin
                    }.toInt()
                gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                setMargins(0, 0, 0, autoBottomMargin)
            } else {
                val rightMargin = context.dpToPx(RIGHT_MARGIN_DP)
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                setMargins(0, 0, rightMargin, 0)
            }
        }
    }

    private fun bindControlSwitcherIfNeeded(view: View) {
        controlSwitcher.isGone = !useControlSwitcher
        syncControlSwitcher(gameViewModel.singleState().selectedAction)

        if (controlSwitcher.parent == null) {
            (view.parent as? FrameLayout)
                ?.addView(controlSwitcher, getSwitchControlLayoutParams())
        }
    }

    private fun initSwitchButtonView(): SwitchButtonView {
        return SwitchButtonView(requireContext()).apply {
            alpha = 0f
            animate().apply {
                alpha(1.0f)
                duration = DELAY_TO_CONTROL_DISPLAY
                start()
            }

            isVisible = true
            layoutParams = getSwitchControlLayoutParams()

            setQuestionButtonVisibility(preferencesRepository.useQuestionMark())

            setOnFlagClickListener {
                gameViewModel.changeSwitchControlAction(Action.SwitchMark)
                gameAudioManager.playSwitchAction()
            }

            setOnOpenClickListener {
                gameViewModel.changeSwitchControlAction(Action.OpenTile)
                gameAudioManager.playSwitchAction()
            }

            setOnQuestionClickListener {
                gameViewModel.changeSwitchControlAction(Action.QuestionMark)
                gameAudioManager.playSwitchAction()
            }
        }
    }

    private fun syncControlSwitcher(action: Action?) {
        if (useControlSwitcher) {
            val isPortrait = requireContext().isPortrait()
            if (isPortrait && controlSwitcher.isVertical()) {
                controlSwitcher.setHorizontalLayout()
                layoutParent?.run {
                    removeView(controlSwitcher)
                    addView(controlSwitcher, getSwitchControlLayoutParams())
                }
            } else if (!isPortrait && controlSwitcher.isHorizontal()) {
                controlSwitcher.setVerticalLayout()
                layoutParent?.run {
                    removeView(controlSwitcher)
                    addView(controlSwitcher, getSwitchControlLayoutParams())
                }
            }

            controlSwitcher.apply {
                when (action) {
                    Action.SwitchMark -> selectFlag()
                    Action.OpenTile -> selectOpen()
                    Action.QuestionMark -> selectQuestionMark()
                    else -> {}
                }
            }
        }
    }

    private fun appBarHeight(context: Context): Float {
        return if (context.isPortrait()) {
            dimensionRepository.actionBarSize().toFloat()
        } else {
            0f
        }
    }

    private fun getInternalPadding(): InternalPadding {
        val padding = dimensionRepository.areaSize()
        return InternalPadding(
            start = padding,
            end = padding,
            bottom = padding,
            top = padding,
        )
    }

    companion object {
        val TAG = GameRenderFragment::class.simpleName

        const val MAX_INVALID_TIME_S = 30
        const val BOTTOM_MARGIN_WITHOUT_NAV_DP = 48
        const val BOTTOM_MARGIN_WITH_NAV_DP = 80
        const val RIGHT_MARGIN_DP = 32
        const val DELAY_TO_CONTROL_DISPLAY = 200L
    }
}
