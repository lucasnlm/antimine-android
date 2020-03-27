package dev.lucasnlm.antimine.common.level.viewmodel

import android.app.Application
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.common.level.LevelFacade
import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackInteractor
import dev.lucasnlm.antimine.core.analytics.AnalyticsManager
import dev.lucasnlm.antimine.core.analytics.models.Analytics
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(
    val application: Application,
    val eventObserver: MutableLiveData<Event>,
    private val savesRepository: ISavesRepository,
    private val dimensionRepository: IDimensionRepository,
    private val preferencesRepository: IPreferencesRepository,
    private val hapticFeedbackInteractor: IHapticFeedbackInteractor,
    private val minefieldRepository: MinefieldRepository,
    private val analyticsManager: AnalyticsManager,
    private val clock: Clock
) : ViewModel() {
    private lateinit var levelFacade: LevelFacade
    private var currentDifficulty: Difficulty = Difficulty.Standard
    private var initialized = false

    val field = MutableLiveData<List<Area>>()
    val fieldRefresh = MutableLiveData<Int>()
    val elapsedTimeSeconds = MutableLiveData<Long>()
    val mineCount = MutableLiveData<Int>()
    val difficulty = MutableLiveData<Difficulty>()
    val levelSetup = MutableLiveData<Minefield>()

    fun startNewGame(difficulty: Difficulty = currentDifficulty): Minefield {
        clock.reset()
        elapsedTimeSeconds.postValue(0L)
        currentDifficulty = difficulty

        val minefield = minefieldRepository.fromDifficulty(
            difficulty, dimensionRepository, preferencesRepository
        )

        levelFacade = LevelFacade(minefield)

        mineCount.postValue(minefield.mines)
        this.difficulty.postValue(difficulty)
        levelSetup.postValue(minefield)
        refreshField()

        eventObserver.postValue(Event.StartNewGame)

        analyticsManager.sentEvent(
            Analytics.NewGame(
                minefield, difficulty,
                levelFacade.seed,
                useAccessibilityMode()
            )
        )

        return minefield
    }

    private fun resumeGameFromSave(save: Save): Minefield {
        clock.reset(save.duration)
        elapsedTimeSeconds.postValue(save.duration)

        val setup = save.minefield
        levelFacade = LevelFacade(save)

        mineCount.postValue(setup.mines)
        difficulty.postValue(save.difficulty)
        levelSetup.postValue(setup)
        refreshField()

        when {
            levelFacade.hasAnyMineExploded() -> eventObserver.postValue(Event.ResumeGameOver)
            levelFacade.checkVictory() -> eventObserver.postValue(Event.ResumeVictory)
            else -> eventObserver.postValue(Event.ResumeGame)
        }

        analyticsManager.sentEvent(Analytics.ResumePreviousGame())
        return setup
    }

    suspend fun onCreate(newGame: Difficulty? = null): Minefield = withContext(Dispatchers.IO) {
        val lastGame = if (newGame == null) savesRepository.fetchCurrentSave() else null

        if (lastGame != null) {
            currentDifficulty = lastGame.difficulty
        } else if (newGame != null) {
            currentDifficulty = newGame
        }

        if (lastGame == null) {
            startNewGame(currentDifficulty)
        } else {
            resumeGameFromSave(lastGame)
        }.also {
            initialized = true
        }
    }

    fun pauseGame() {
        if (initialized) {
            if (levelFacade.hasMines) {
                eventObserver.postValue(Event.Pause)
            }
            clock.stop()
        }
    }

    suspend fun saveGame() {
        if (initialized && levelFacade.hasMines) {
            val id = savesRepository.saveGame(
                levelFacade.getSaveState(elapsedTimeSeconds.value ?: 0L, currentDifficulty)
            )
            levelFacade.setCurrentSaveId(id?.toInt() ?: 0)
        }
    }

    fun resumeGame() {
        if (initialized) {
            if (levelFacade.hasMines) {
                eventObserver.postValue(Event.Resume)
            }
        }
    }

    fun onLongClick(index: Int) {
        levelFacade.turnOffAllHighlighted()
        refreshField()

        if (levelFacade.hasCoverOn(index)) {
            levelFacade.switchMarkAt(index).run {
                refreshField(this)
                hapticFeedbackInteractor.toggleFlagFeedback()
            }

            analyticsManager.sentEvent(Analytics.LongPressArea(index))
        } else {
            levelFacade.openNeighbors(index).forEach { refreshField(it) }

            analyticsManager.sentEvent(Analytics.LongPressMultipleArea(index))
        }

        updateGameState()
    }

    fun onClickArea(index: Int) {
        levelFacade.turnOffAllHighlighted()

        if (levelFacade.hasMarkOn(index)) {
            levelFacade.removeMark(index).run {
                refreshField(this)
            }
            hapticFeedbackInteractor.toggleFlagFeedback()
        } else {
            if (!levelFacade.hasMines) {
                levelFacade.plantMinesExcept(index, true)
            }

            levelFacade.clickArea(index).apply {
                refreshField(this)
            }
        }

        if (preferencesRepository.useFlagAssistant() && !levelFacade.hasAnyMineExploded()) {
            levelFacade.runFlagAssistant().forEach {
                Handler().post {
                    refreshField(it)
                }
            }
        }

        updateGameState()
        analyticsManager.sentEvent(Analytics.PressArea(index))
    }

    private fun refreshMineCount() = mineCount.postValue(levelFacade.remainingMines())

    private fun updateGameState() {
        when {
            levelFacade.hasAnyMineExploded() -> {
                hapticFeedbackInteractor.explosionFeedback()
                eventObserver.postValue(Event.GameOver)
            }
            else -> {
                eventObserver.postValue(Event.Running)
            }
        }

        if (levelFacade.hasMines) {
            refreshMineCount()
        }

        if (levelFacade.checkVictory()) {
            eventObserver.postValue(Event.Victory)
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

    suspend fun gameOver() {
        levelFacade.run {
            analyticsManager.sentEvent(Analytics.GameOver(clock.time(), getScore()))

            findExplodedMine()?.let { exploded ->
                takeExplosionRadius(exploded).forEach {
                    it.isCovered = false
                    refreshField(it)
                    delay(75L)
                }
            }

            showWrongFlags()
            refreshField()
            updateGameState()
        }

        GlobalScope.launch {
            saveGame()
        }
    }

    fun victory() {
        levelFacade.run {
            analyticsManager.sentEvent(
                Analytics.Victory(
                    clock.time(),
                    getScore(),
                    currentDifficulty
                )
            )
            flagAllMines()
            showWrongFlags()
        }

        GlobalScope.launch {
            saveGame()
        }
    }

    fun useAccessibilityMode() = preferencesRepository.useLargeAreas()

    private fun refreshField(area: Area? = null) {
        when {
            area == null -> field.postValue(levelFacade.field.toList())
            !area.isCovered && area.minesAround != 0 -> field.postValue(levelFacade.field.toList())
            area.safeZone -> field.postValue(levelFacade.field.toList())
            else -> fieldRefresh.postValue(area.id)
        }
    }
}
