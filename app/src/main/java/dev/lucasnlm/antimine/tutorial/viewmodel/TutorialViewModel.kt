package dev.lucasnlm.antimine.tutorial.viewmodel

import android.content.Context
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.common.level.repository.ITipRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackManager
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.sound.ISoundManager
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.antimine.tutorial.view.TutorialField
import dev.lucasnlm.external.IFeatureFlagManager
import dev.lucasnlm.external.IPlayGamesManager
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.MutableStateFlow

class TutorialViewModel(
    savesRepository: ISavesRepository,
    statsRepository: IStatsRepository,
    dimensionRepository: IDimensionRepository,
    themeRepository: IThemeRepository,
    soundManager: ISoundManager,
    minefieldRepository: IMinefieldRepository,
    analyticsManager: IAnalyticsManager,
    playGamesManager: IPlayGamesManager,
    featureFlagManager: IFeatureFlagManager,
    tipRepository: ITipRepository,
    private val clock: Clock,
    private val context: Context,
    private val hapticFeedbackManager: IHapticFeedbackManager,
    private val preferencesRepository: IPreferencesRepository,
) : GameViewModel(
    savesRepository,
    statsRepository,
    dimensionRepository,
    preferencesRepository,
    hapticFeedbackManager,
    themeRepository,
    soundManager,
    minefieldRepository,
    analyticsManager,
    playGamesManager,
    tipRepository,
    featureFlagManager,
    clock,
) {
    val tutorialState = MutableStateFlow(
        TutorialState(
            0,
            context.getString(R.string.tutorial),
            context.getString(R.string.tutorial_0_bottom, openActionLabel()),
            completed = false,
        )
    )

    val shake = ConflatedBroadcastChannel<Unit>()

    init {
        field.postValue(TutorialField.getStep0())
        analyticsManager.sentEvent(Analytics.TutorialStarted)
    }

    private fun currentStep(): Int {
        return tutorialState.value.step
    }

    fun openActionLabel(): String =
        when (preferencesRepository.controlStyle()) {
            ControlStyle.Standard -> context.getString(R.string.single_click)
            ControlStyle.SwitchMarkOpen -> context.getString(R.string.single_click)
            ControlStyle.FastFlag -> context.getString(R.string.long_press)
            ControlStyle.DoubleClick -> context.getString(R.string.double_click)
            ControlStyle.DoubleClickInverted -> context.getString(R.string.single_click)
        }

    fun flagActionLabel(): String =
        when (preferencesRepository.controlStyle()) {
            ControlStyle.Standard -> context.getString(R.string.long_press)
            ControlStyle.SwitchMarkOpen -> context.getString(R.string.long_press)
            ControlStyle.FastFlag -> context.getString(R.string.single_click)
            ControlStyle.DoubleClick -> context.getString(R.string.single_click)
            ControlStyle.DoubleClickInverted -> context.getString(R.string.double_click)
        }

    private fun postStep(step: List<Area>, top: String, bottom: String, completed: Boolean = false) {
        field.postValue(step)
        clock.stop()
        tutorialState.value = tutorialState.value.copy(
            completed = completed,
            step = currentStep() + 1,
            topMessage = top,
            bottomMessage = bottom,
        )
    }

    private fun openTileAction(index: Int) {
        when (currentStep()) {
            0 -> {
                postStep(
                    TutorialField.getStep1(),
                    context.getString(R.string.tutorial_1_top),
                    context.getString(R.string.tutorial_1_bottom, flagActionLabel()),
                )
            }
            2 -> {
                if (index == 15) {
                    postStep(
                        TutorialField.getStep3(),
                        context.getString(R.string.tutorial_3_top),
                        context.getString(R.string.tutorial_3_bottom),
                    )
                }
            }
            3 -> {
                if (index == 20 || index == 21) {
                    postStep(
                        TutorialField.getStep4(),
                        context.getString(R.string.tutorial_4_top),
                        context.getString(R.string.tutorial_4_bottom, flagActionLabel()),
                    )
                }
            }
            5 -> {
                if (index == 23) {
                    postStep(
                        TutorialField.getStep6(),
                        context.getString(R.string.tutorial_5_top),
                        context.getString(R.string.tutorial_5_bottom, openActionLabel(), flagActionLabel()),
                    )
                }
            }
            6 -> {
                if (index == 24 || index == 19 || index == 14) {
                    postStep(
                        TutorialField.getStep7(),
                        context.getString(R.string.tutorial_5_top),
                        context.getString(R.string.tutorial_5_bottom, openActionLabel(), flagActionLabel()),
                    )
                }
            }
            else -> {
                hapticFeedbackManager.tutorialErrorFeedback()
                shake.offer(Unit)
            }
        }
    }

    private fun longTileAction(index: Int) {
        when (currentStep()) {
            1 -> {
                if (index == 10) {
                    postStep(
                        TutorialField.getStep2(),
                        context.getString(R.string.tutorial_2_top),
                        context.getString(R.string.tutorial_2_bottom, openActionLabel()),
                    )
                }
            }
            4 -> {
                if (index == 22) {
                    postStep(
                        TutorialField.getStep5(),
                        context.getString(R.string.tutorial_5_top),
                        context.getString(R.string.tutorial_5_bottom, openActionLabel(), flagActionLabel()),
                    )
                }
            }
            7 -> {
                if (index == 9) {
                    postStep(
                        TutorialField.getStep8(),
                        context.getString(R.string.tutorial_5_top),
                        context.getString(R.string.tutorial_5_bottom, openActionLabel(), flagActionLabel()),
                    )
                }
            }
            8 -> {
                if (index == 4) {
                    postStep(
                        TutorialField.getStep9(),
                        context.getString(R.string.tutorial_5_top),
                        context.getString(R.string.tutorial_5_bottom, openActionLabel(), flagActionLabel()),
                        completed = true
                    )
                }
            }
            else -> {
                hapticFeedbackManager.tutorialErrorFeedback()
                shake.offer(Unit)
            }
        }
    }

    override suspend fun onDoubleClick(index: Int) {
        clock.stop()
        when (preferencesRepository.controlStyle()) {
            ControlStyle.DoubleClick -> openTileAction(index)
            ControlStyle.DoubleClickInverted -> longTileAction(index)
            else -> {}
        }
    }

    override suspend fun onSingleClick(index: Int) {
        clock.stop()
        when (preferencesRepository.controlStyle()) {
            ControlStyle.SwitchMarkOpen -> openTileAction(index)
            ControlStyle.Standard -> openTileAction(index)
            ControlStyle.FastFlag -> longTileAction(index)
            ControlStyle.DoubleClick -> longTileAction(index)
            ControlStyle.DoubleClickInverted -> openTileAction(index)
        }
    }

    override suspend fun onLongClick(index: Int) {
        clock.stop()
        when (preferencesRepository.controlStyle()) {
            ControlStyle.SwitchMarkOpen -> longTileAction(index)
            ControlStyle.Standard -> longTileAction(index)
            ControlStyle.FastFlag -> openTileAction(index)
            else -> {}
        }
    }
}
