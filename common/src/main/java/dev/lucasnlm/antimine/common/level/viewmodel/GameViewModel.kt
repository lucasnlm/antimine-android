package dev.lucasnlm.antimine.common.level.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.GameController
import dev.lucasnlm.antimine.common.level.database.models.FirstOpen
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.common.level.models.StateUpdate
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackManager
import dev.lucasnlm.antimine.core.analytics.AnalyticsManager
import dev.lucasnlm.antimine.core.analytics.models.Analytics
import dev.lucasnlm.antimine.core.control.ActionResponse
import dev.lucasnlm.antimine.core.control.GameControl
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.sound.ISoundManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel @ViewModelInject constructor(
    private val savesRepository: ISavesRepository,
    private val statsRepository: IStatsRepository,
    private val dimensionRepository: IDimensionRepository,
    private val preferencesRepository: IPreferencesRepository,
    private val hapticFeedbackManager: IHapticFeedbackManager,
    private val soundManager: ISoundManager,
    private val minefieldRepository: IMinefieldRepository,
    private val analyticsManager: AnalyticsManager,
    private val clock: Clock
) : ViewModel() {
    val eventObserver = MutableLiveData<Event>()
    val retryObserver = MutableLiveData<Unit>()
    val shareObserver = MutableLiveData<Unit>()

    private lateinit var gameController: GameController
    private var currentDifficulty: Difficulty = Difficulty.Standard
    private var initialized = false

    val field = MutableLiveData<List<Area>>()
    val fieldRefresh = MutableLiveData<Int>()
    val elapsedTimeSeconds = MutableLiveData<Long>()
    val mineCount = MutableLiveData<Int>()
    val difficulty = MutableLiveData<Difficulty>()
    val levelSetup = MutableLiveData<Minefield>()
    val saveId = MutableLiveData<Long>()

    fun startNewGame(newDifficulty: Difficulty = currentDifficulty): Minefield {
        clock.reset()
        elapsedTimeSeconds.postValue(0L)
        currentDifficulty = newDifficulty

        val minefield = minefieldRepository.fromDifficulty(
            newDifficulty, dimensionRepository, preferencesRepository
        )

        gameController = GameController(minefield, minefieldRepository.randomSeed())
        refreshUserPreferences()

        mineCount.postValue(minefield.mines)
        difficulty.postValue(newDifficulty)
        levelSetup.postValue(minefield)
        refreshAll()

        eventObserver.postValue(Event.StartNewGame)

        analyticsManager.sentEvent(
            Analytics.NewGame(
                minefield, newDifficulty,
                gameController.seed,
                useAccessibilityMode()
            )
        )

        return minefield
    }

    private fun resumeGameFromSave(save: Save): Minefield {
        clock.reset(save.duration)
        elapsedTimeSeconds.postValue(save.duration)

        val setup = save.minefield
        gameController = GameController(save)
        refreshUserPreferences()

        mineCount.postValue(setup.mines)
        difficulty.postValue(save.difficulty)
        levelSetup.postValue(setup)
        refreshAll()
        refreshMineCount()

        when {
            gameController.hasAnyMineExploded() -> eventObserver.postValue(Event.GameOver)
            gameController.checkVictory() -> eventObserver.postValue(Event.Victory)
            else -> eventObserver.postValue(Event.ResumeGame)
        }

        saveId.postValue(save.uid.toLong())
        analyticsManager.sentEvent(Analytics.ResumePreviousGame)
        return setup
    }

    private fun retryGame(save: Save): Minefield {
        clock.reset()
        elapsedTimeSeconds.postValue(0L)
        currentDifficulty = save.difficulty

        val setup = save.minefield
        gameController = GameController(setup, save.seed, save.uid).apply {
            if (save.firstOpen is FirstOpen.Position) {
                singleClick(save.firstOpen.value)
            }
        }
        refreshUserPreferences()

        mineCount.postValue(setup.mines)
        difficulty.postValue(save.difficulty)
        levelSetup.postValue(setup)
        refreshAll()

        eventObserver.postValue(Event.StartNewGame)

        analyticsManager.sentEvent(
            Analytics.RetryGame(
                setup,
                currentDifficulty,
                gameController.seed,
                useAccessibilityMode(),
                save.firstOpen.toInt()
            )
        )

        saveId.postValue(save.uid.toLong())
        return setup
    }

    suspend fun loadGame(uid: Int): Minefield = withContext(Dispatchers.IO) {
        val lastGame = savesRepository.loadFromId(uid)

        if (lastGame != null) {
            saveId.postValue(uid.toLong())
            currentDifficulty = lastGame.difficulty
            resumeGameFromSave(lastGame)
        } else {
            // Fail to load
            startNewGame()
        }.also {
            initialized = true
        }
    }

    suspend fun retryGame(uid: Int): Minefield = withContext(Dispatchers.IO) {
        val save = savesRepository.loadFromId(uid)

        if (save != null) {
            saveId.postValue(uid.toLong())
            currentDifficulty = save.difficulty
            retryGame(save)
        } else {
            // Fail to load
            startNewGame()
        }.also {
            initialized = true
        }
    }

    suspend fun loadLastGame(): Minefield = withContext(Dispatchers.IO) {
        val lastGame = savesRepository.fetchCurrentSave()

        if (lastGame != null) {
            saveId.postValue(lastGame.uid.toLong())
            currentDifficulty = lastGame.difficulty
            resumeGameFromSave(lastGame)
        } else {
            // Fail to load
            startNewGame()
        }.also {
            initialized = true
        }
    }

    fun pauseGame() {
        if (initialized) {
            if (gameController.hasMines) {
                eventObserver.postValue(Event.Pause)
            }
            clock.stop()
        }
    }

    suspend fun saveGame() {
        if (initialized && gameController.hasMines) {
            val id = savesRepository.saveGame(
                gameController.getSaveState(elapsedTimeSeconds.value ?: 0L, currentDifficulty)
            )
            gameController.setCurrentSaveId(id?.toInt() ?: 0)
            saveId.postValue(id)
        }
    }

    private suspend fun saveStats() {
        if (initialized && gameController.hasMines) {
            gameController.getStats(elapsedTimeSeconds.value ?: 0L)?.let {
                statsRepository.addStats(it)
            }
        }
    }

    fun resumeGame() {
        if (initialized && gameController.hasMines && !gameController.isGameOver()) {
            eventObserver.postValue(Event.Resume)
            runClock()
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    suspend fun onLongClick(index: Int) {
        gameController.longPress(index).flatMapConcat { (action, flow) ->
            onFeedbackAnalytics(action, index)
            flow
        }.collect {
            if (it is StateUpdate.Multiple) {
                refreshAll()
            } else if (it is StateUpdate.Single) {
                refreshIndex(it.index, false)
            }
        }.also {
            onPostAction()
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    suspend fun onDoubleClickArea(index: Int) {
        gameController.doubleClick(index).flatMapConcat { (action, flow) ->
            onFeedbackAnalytics(action, index)
            flow
        }.collect {
            if (it is StateUpdate.Multiple) {
                refreshAll()
            } else if (it is StateUpdate.Single) {
                refreshIndex(it.index, false)
            }
        }.also {
            onPostAction()

            if (preferencesRepository.useHapticFeedback()) {
                hapticFeedbackManager.longPressFeedback()
            }
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    suspend fun onSingleClick(index: Int) {
        gameController.singleClick(index).flatMapConcat { (action, flow) ->
            onFeedbackAnalytics(action, index)
            flow
        }.collect {
            if (it is StateUpdate.Multiple) {
                refreshAll()
            } else if (it is StateUpdate.Single) {
                refreshIndex(it.index, false)
            }
        }.also {
            onPostAction()
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun onPostAction() {
        if (preferencesRepository.useFlagAssistant() && !gameController.hasAnyMineExploded()) {
            gameController.runFlagAssistant().collect {
                refreshIndex(it)
            }
        }

        updateGameState()
    }

    private fun onFeedbackAnalytics(action: ActionResponse, index: Int) {
        when (action) {
            ActionResponse.OpenTile -> {
                analyticsManager.sentEvent(Analytics.OpenTile(index))
            }
            ActionResponse.SwitchMark -> {
                analyticsManager.sentEvent(Analytics.SwitchMark(index))
            }
            ActionResponse.HighlightNeighbors -> {
                analyticsManager.sentEvent(Analytics.HighlightNeighbors(index))
            }
            ActionResponse.OpenNeighbors -> {
                analyticsManager.sentEvent(Analytics.OpenNeighbors(index))
            }
        }
    }

    private fun refreshMineCount() = mineCount.postValue(gameController.remainingMines())

    private fun updateGameState() {
        when {
            gameController.hasAnyMineExploded() -> {
                eventObserver.postValue(Event.GameOver)
            }
            else -> {
                eventObserver.postValue(Event.Running)
            }
        }

        if (gameController.hasMines) {
            refreshMineCount()
        }

        if (gameController.checkVictory()) {
            refreshAll()
            eventObserver.postValue(Event.Victory)
        }
    }

    fun refreshUserPreferences() {
        val questionMark = preferencesRepository.useQuestionMark()
        val controlType = preferencesRepository.controlStyle()
        val gameControl = GameControl.fromControlType(controlType)
        gameController.apply {
            updateGameControl(gameControl)
            useQuestionMark(questionMark)
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

    fun revealAllEmptyAreas() = gameController.revealAllEmptyAreas()

    fun explosionDelay() = if (preferencesRepository.useAnimations()) 750L else 0L

    suspend fun gameOver(fromResumeGame: Boolean) {
        gameController.run {
            analyticsManager.sentEvent(Analytics.GameOver(clock.time(), getScore()))
            val explosionTime = (explosionDelay() / gameController.getMinesCount().coerceAtLeast(10))
            val delayMillis = explosionTime.coerceAtLeast(25L)

            if (!fromResumeGame) {
                if (preferencesRepository.useHapticFeedback()) {
                    hapticFeedbackManager.explosionFeedback()
                }

                if (preferencesRepository.isSoundEffectsEnabled()) {
                    soundManager.play(R.raw.mine_explosion_sound)
                }
            }

            findExplodedMine()?.let { exploded ->
                takeExplosionRadius(exploded).forEach {
                    it.isCovered = false
                    refreshIndex(it.id)
                    delay(delayMillis)
                }
            }

            showWrongFlags()
            refreshAll()
            updateGameState()
        }

        GlobalScope.launch {
            saveStats()
            saveGame()
        }
    }

    fun victory() {
        gameController.run {
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
            saveStats()
            saveGame()
        }
    }

    fun useAccessibilityMode() = preferencesRepository.useLargeAreas()

    private fun refreshIndex(targetIndex: Int, multipleChanges: Boolean = false) {
        if (!preferencesRepository.useAnimations() || multipleChanges) {
            field.postValue(gameController.field)
        } else {
            fieldRefresh.postValue(targetIndex)
        }
    }

    private fun refreshAll() {
        field.postValue(gameController.field)
    }
}
