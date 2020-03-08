package dev.lucasnlm.antimine.common.level.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.common.level.GameModeFactory
import dev.lucasnlm.antimine.common.level.LevelFacade
import dev.lucasnlm.antimine.common.level.data.*
import dev.lucasnlm.antimine.common.level.database.data.Save
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackInteractor
import dev.lucasnlm.antimine.core.analytics.AnalyticsManager
import dev.lucasnlm.antimine.core.analytics.Event
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(
    val application: Application,
    val eventObserver: MutableLiveData<GameEvent>,
    private val savesRepository: ISavesRepository,
    private val dimensionRepository: IDimensionRepository,
    private val preferencesRepository: IPreferencesRepository,
    private val hapticFeedbackInteractor: IHapticFeedbackInteractor,
    private val analyticsManager: AnalyticsManager,
    private val clock: Clock
) : ViewModel() {
    private lateinit var levelFacade: LevelFacade
    private var currentDifficulty: DifficultyPreset = DifficultyPreset.Standard
    private var initialized = false

    val field = MutableLiveData<List<Area>>()
    val fieldRefresh = MutableLiveData<Int>()
    val elapsedTimeSeconds = MutableLiveData<Long>()
    val mineCount = MutableLiveData<Int>()
    val difficulty = MutableLiveData<DifficultyPreset>()
    val levelSetup = MutableLiveData<LevelSetup>()

    private fun startNewGame(gameId: Int, difficultyPreset: DifficultyPreset): LevelSetup {
        clock.reset()
        elapsedTimeSeconds.postValue(0L)
        currentDifficulty = difficultyPreset

        val setup = GameModeFactory.fromDifficultyPreset(
            difficultyPreset, dimensionRepository, preferencesRepository
        )

        levelFacade = LevelFacade(gameId, setup)

        mineCount.postValue(setup.mines)
        difficulty.postValue(difficultyPreset)
        levelSetup.postValue(setup)
        field.postValue(levelFacade.field.toList())

        eventObserver.postValue(GameEvent.StartNewGame)

        analyticsManager.sentEvent(Event.NewGame(setup, levelFacade.seed, useAccessibilityMode()))

        return setup
    }

    private fun resumeGameFromSave(save: Save): LevelSetup {
        clock.reset(save.duration)
        elapsedTimeSeconds.postValue(save.duration)

        val setup = save.levelSetup
        levelFacade = LevelFacade(save)

        mineCount.postValue(setup.mines)
        difficulty.postValue(save.levelSetup.preset)
        levelSetup.postValue(setup)
        field.postValue(levelFacade.field.toList())

        when {
            levelFacade.hasAnyMineExploded() -> eventObserver.postValue(GameEvent.ResumeGameOver)
            levelFacade.checkVictory() -> eventObserver.postValue(GameEvent.ResumeVictory)
            else -> eventObserver.postValue(GameEvent.ResumeGame)
        }

        analyticsManager.sentEvent(Event.ResumePreviousGame())

        return setup
    }

    suspend fun startNewGame(difficultyPreset: DifficultyPreset = currentDifficulty): LevelSetup =
        withContext(Dispatchers.IO) {
            val newGameId = savesRepository.getNewSaveId()
            startNewGame(newGameId, difficultyPreset)
        }

    suspend fun onCreate(newGame: DifficultyPreset? = null): LevelSetup = withContext(Dispatchers.IO) {
        val lastGame = if (newGame == null) savesRepository.fetchCurrentSave() else null

        if (lastGame != null) {
            currentDifficulty = lastGame.levelSetup.preset
        } else if (newGame != null) {
            currentDifficulty = newGame
        }

        if (lastGame == null) {
            val newGameId = savesRepository.getNewSaveId()
            startNewGame(newGameId, currentDifficulty)
        } else {
            resumeGameFromSave(lastGame)
        }.also {
            initialized = true
        }
    }

    fun pauseGame() {
        if (initialized) {
            if (levelFacade.hasMines) {
                eventObserver.postValue(GameEvent.Pause)
            }
            clock.stop()
        }
    }

    suspend fun saveGame() {
        if (initialized) {
            if (levelFacade.hasMines) {
                savesRepository.saveGame(
                    levelFacade.getSaveState().copy(duration = elapsedTimeSeconds.value ?: 0L)
                )
            }
        }
    }

    fun resumeGame() {
        if (initialized) {
            if (levelFacade.hasMines) {
                eventObserver.postValue(GameEvent.Resume)
            }
        }
    }

    fun onLongClick(index: Int) {
        levelFacade.turnOffAllHighlighted()

        if (levelFacade.hasCoverOn(index)) {
            if (levelFacade.switchMarkAt(index)) {
                refreshField(index)
                hapticFeedbackInteractor.toggleFlagFeedback()
            }

            analyticsManager.sentEvent(Event.LongPressArea(index))
        } else {
            levelFacade.openNeighbors(index)

            analyticsManager.sentEvent(Event.LongPressMultipleArea(index))
        }

        field.postValue(levelFacade.field.toList())

        refreshGame()
    }

    fun onClickArea(index: Int) {
        levelFacade.turnOffAllHighlighted()

        if (levelFacade.hasMarkOn(index)) {
            levelFacade.removeMark(index)
            hapticFeedbackInteractor.toggleFlagFeedback()
            refreshField(index)
        } else {
            if (!levelFacade.hasMines) {
                levelFacade.plantMinesExcept(index, true)
            }

            levelFacade.clickArea(index)

            field.postValue(levelFacade.field.toList())
        }

        if (preferencesRepository.useFlagAssistant() && !levelFacade.hasAnyMineExploded()) {
            levelFacade.runFlagAssistant()
        }

        refreshGame()
        analyticsManager.sentEvent(Event.PressArea(index))
    }

    private fun refreshMineCount() = mineCount.postValue(levelFacade.remainingMines())

    private fun refreshGame() {
        when {
            levelFacade.hasAnyMineExploded() -> {
                hapticFeedbackInteractor.explosionFeedback()
                eventObserver.postValue(GameEvent.GameOver)
            }
            else -> {
                eventObserver.postValue(GameEvent.Running)
            }
        }

        if (levelFacade.hasMines) {
            refreshMineCount()
        }

        if (levelFacade.checkVictory()) {
            eventObserver.postValue(GameEvent.Victory)
        }
    }

    fun runClock() {
        clock.run {
            if (isStopped) start {
                elapsedTimeSeconds.postValue(it)
            }
        }
    }

    fun stopClock() {
        clock.stop()
    }

    fun revealAllEmptyAreas() {
        levelFacade.revealAllEmptyAreas()
    }

    fun gameOver() {
        levelFacade.run {
            analyticsManager.sentEvent(Event.GameOver(clock.time(), getStats()))
            showAllMines()
            showWrongFlags()
        }

        GlobalScope.launch {
            saveGame()
        }
    }

    fun victory() {
        levelFacade.run {
            analyticsManager.sentEvent(Event.Victory(clock.time(), getStats(), currentDifficulty))
            flagAllMines()
            showWrongFlags()
        }

        GlobalScope.launch {
            saveGame()
        }
    }

    fun useAccessibilityMode() = preferencesRepository.useLargeAreas()

    private fun refreshField(index: Int) {
        fieldRefresh.postValue(index)
    }
}
