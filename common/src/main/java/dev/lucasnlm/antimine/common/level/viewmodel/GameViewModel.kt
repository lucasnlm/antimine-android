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
import dev.lucasnlm.antimine.core.analytics.IAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.models.Analytics
import dev.lucasnlm.antimine.core.control.ActionResponse
import dev.lucasnlm.antimine.core.control.ControlStyle
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
    private val analyticsManager: IAnalyticsManager,
    private val clock: Clock
) : ViewModel() {
    val eventObserver = MutableLiveData<Event>()
    val retryObserver = MutableLiveData<Unit>()
    val shareObserver = MutableLiveData<Unit>()

    private lateinit var gameController: GameController
    private var initialized = false
    private var currentDifficulty: Difficulty = Difficulty.Standard

    val field = MutableLiveData<List<Area>>()
    val fieldRefresh = MutableLiveData<Int>()
    val elapsedTimeSeconds = MutableLiveData<Long>()
    val mineCount = MutableLiveData<Int>()
    val difficulty = MutableLiveData<Difficulty>()
    val saveId = MutableLiveData<Long>()

    fun getMinefieldInfo(): Minefield {
        return gameController.minefield
    }

    fun startNewGame(newDifficulty: Difficulty = currentDifficulty): Minefield {
        clock.reset()
        elapsedTimeSeconds.postValue(0L)
        currentDifficulty = newDifficulty

        val minefield = minefieldRepository.fromDifficulty(
            newDifficulty, dimensionRepository, preferencesRepository
        )

        gameController = GameController(
            minefield,
            minefieldRepository.randomSeed(),
            dimensionRepository.isRoundDevice()
        )
        gameController = GameController(minefield, minefieldRepository.randomSeed())
        initialized = true
        refreshUserPreferences()

        mineCount.postValue(minefield.mines)
        difficulty.postValue(newDifficulty)
        refreshAll()

        eventObserver.postValue(Event.StartNewGame)

        analyticsManager.sentEvent(
            Analytics.NewGame(
                minefield, newDifficulty,
                gameController.seed,
                getAreaSizeMultiplier()
            )
        )

        return minefield
    }

    private fun resumeGameFromSave(save: Save): Minefield {
        clock.reset(save.duration)
        elapsedTimeSeconds.postValue(save.duration)

        val setup = save.minefield
        gameController = GameController(save)
        initialized = true
        refreshUserPreferences()

        mineCount.postValue(setup.mines)
        difficulty.postValue(save.difficulty)
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

    private fun retryGame(save: Save) {
        clock.reset()
        elapsedTimeSeconds.postValue(0L)
        currentDifficulty = save.difficulty
        gameController = GameController(save)
        initialized = true
        refreshUserPreferences()

        mineCount.postValue(save.minefield.mines)
        difficulty.postValue(save.difficulty)
        refreshAll()

        eventObserver.postValue(Event.StartNewGame)

        analyticsManager.sentEvent(
            Analytics.RetryGame(
                save.minefield,
                currentDifficulty,
                gameController.seed,
                getAreaSizeMultiplier(),
                save.firstOpen.toInt()
            )
        )

        saveId.postValue(save.uid.toLong())
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
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    suspend fun retryGame(uid: Int): Minefield = withContext(Dispatchers.IO) {
        val save = savesRepository.loadFromId(uid)

        if (save != null) {
            saveId.postValue(uid.toLong())
            currentDifficulty = save.difficulty
            retryGame(save)

            withContext(Dispatchers.Main) {
                if (save.firstOpen is FirstOpen.Position) {
                    gameController.singleClick(save.firstOpen.value).flatMapConcat { it.second }.collect {
                        refreshAll()
                    }
                }
            }

            save.minefield
        } else {
            // Fail to load
            startNewGame()
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

    fun useCustomPreferences(useQuestionMark: Boolean, controlStyle: ControlStyle) {
        gameController.apply {
            val gameControl = GameControl.fromControlType(controlStyle)

            updateGameControl(gameControl)
            useQuestionMark(useQuestionMark)
            useSolverAlgorithms(true)
        }
    }

    fun refreshUserPreferences() {
        if (initialized) {
            gameController.apply {
                val controlType = preferencesRepository.controlStyle()
                val gameControl = GameControl.fromControlType(controlType)

                updateGameControl(gameControl)
                useQuestionMark(preferencesRepository.useQuestionMark())
            }
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

    private fun getAreaSizeMultiplier() = preferencesRepository.areaSizeMultiplier()

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
