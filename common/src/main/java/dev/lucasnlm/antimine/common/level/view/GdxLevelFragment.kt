package dev.lucasnlm.antimine.common.level.view

import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.Resources.getSystem
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.IAppVersionManager
import dev.lucasnlm.antimine.core.dpToPx
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.gdx.GameApplicationListener
import dev.lucasnlm.antimine.gdx.models.RenderQuality
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.ext.toInvertedAndroidColor
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.external.ICrashReporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.system.exitProcess

open class GdxLevelFragment : AndroidFragmentApplication() {
    private val gameViewModel by sharedViewModel<GameViewModel>()
    private val themeRepository: IThemeRepository by inject()
    private val dimensionRepository: IDimensionRepository by inject()
    private val preferencesRepository: IPreferencesRepository by inject()
    private val crashReporter: ICrashReporter by inject()
    private val appVersionManager: IAppVersionManager by inject()

    private var controlSwitcher: MaterialButton? = null

    val Int.px: Int get() = (this * getSystem().displayMetrics.density).toInt()

    private val levelApplicationListener by lazy {
        GameApplicationListener(
            context = requireContext(),
            appVersion = appVersionManager,
            theme = themeRepository.getTheme(),
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

                if (it.isActive && !it.isGameCompleted) {
                    levelApplicationListener.setActionsEnabled(true)
                } else {
                    bindControlSwitcherIfNeeded(view, false)
                    levelApplicationListener.setActionsEnabled(false)
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            gameViewModel.observeState()
                .map { it.seed }
                .distinctUntilChanged()
                .collect {
                    setOpenControlSwitcherIcon()

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
        val width = Resources.getSystem().displayMetrics.widthPixels
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

    private fun setOpenControlSwitcherIcon() {
        controlSwitcher?.apply {
            contentDescription = getString(R.string.open)
            TooltipCompat.setTooltipText(this, getString(R.string.open))
            gameViewModel.refreshUseOpenOnSwitchControl(true)
            preferencesRepository.setSwitchControl(true)
            setIconResource(R.drawable.touch)
        }
    }

    private fun setFlagControlSwitcherIcon() {
        controlSwitcher?.apply {
            contentDescription = getString(R.string.flag_tile)
            TooltipCompat.setTooltipText(this, getString(R.string.flag_tile))
            gameViewModel.refreshUseOpenOnSwitchControl(false)
            preferencesRepository.setSwitchControl(false)
            setIconResource(R.drawable.flag_black)
        }
    }

    private fun toggleControlSwitcherIcon() {
        if (preferencesRepository.openUsingSwitchControl()) {
            setFlagControlSwitcherIcon()
        } else {
            setOpenControlSwitcherIcon()
        }
    }

    private fun bindControlSwitcherIfNeeded(view: View, delayed: Boolean = true) {
        if (controlSwitcher != null) {
            if (preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen) {
                if (preferencesRepository.showToggleButtonOnTopBar()) {
                    controlSwitcher?.visibility = View.GONE
                } else {
                    controlSwitcher?.visibility = View.VISIBLE
                }
            }
        } else if (!preferencesRepository.showToggleButtonOnTopBar()) {
            view.postDelayed(if (delayed) 200L else 0L) {
                val isParentFinishing = activity?.isFinishing ?: true
                if (preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen && !isParentFinishing) {
                    (view.parent as? FrameLayout)?.apply {

                        controlSwitcher = ExtendedFloatingActionButton(context).apply {
                            val palette = themeRepository.getTheme().palette
                            contentDescription = getString(R.string.open)
                            TooltipCompat.setTooltipText(this, getString(R.string.open))
                            gameViewModel.refreshUseOpenOnSwitchControl(true)
                            preferencesRepository.setSwitchControl(true)
                            backgroundTintList = ColorStateList.valueOf(palette.accent.toAndroidColor(255))
                            cornerRadius = 10.px
                            strokeColor = ColorStateList.valueOf(palette.background.toAndroidColor(255))
                            strokeWidth = 1.px
                            setIconResource(R.drawable.touch)
                            elevation = 2f
                            alpha = 0f
                            animate().apply {
                                alpha(1.0f)
                                duration = 300L
                                start()
                            }

                            setOnClickListener {
                                toggleControlSwitcherIcon()
                            }
                        }

                        val layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            val padding = context.dpToPx(24)
                            val leftHanded = preferencesRepository.leftHandedMode()

                            gravity = if (leftHanded) {
                                GravityCompat.START or Gravity.BOTTOM
                            } else {
                                GravityCompat.END or Gravity.BOTTOM
                            }

                            if (leftHanded) {
                                setMargins(
                                    padding + dimensionRepository.horizontalNavigationBarHeight(),
                                    0,
                                    0,
                                    padding + dimensionRepository.verticalNavigationBarHeight()
                                )
                            } else {
                                setMargins(
                                    0,
                                    0,
                                    padding + dimensionRepository.horizontalNavigationBarHeight(),
                                    padding + dimensionRepository.verticalNavigationBarHeight()
                                )
                            }
                        }

                        addView(controlSwitcher, layoutParams)
                    }
                }
            }
        }
    }

    companion object {
        val TAG = GdxLevelFragment::class.simpleName
    }
}
