package dev.lucasnlm.antimine.common.level.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.GameController
import dev.lucasnlm.antimine.common.level.database.models.FirstOpen
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackManager
import dev.lucasnlm.antimine.core.analytics.IAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.models.Analytics
import dev.lucasnlm.antimine.core.control.ActionResponse
import dev.lucasnlm.antimine.core.control.GameControl
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.sound.ISoundManager
import dev.lucasnlm.antimine.core.themes.model.AppTheme
import dev.lucasnlm.antimine.core.themes.repository.IThemeRepository
import dev.lucasnlm.external.Achievement
import dev.lucasnlm.external.IPlayGamesManager
import dev.lucasnlm.external.Leaderboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(
    private val savesRepository: ISavesRepository,
    private val statsRepository: IStatsRepository,
    private val dimensionRepository: IDimensionRepository,
    private val preferencesRepository: IPreferencesRepository,
    private val hapticFeedbackManager: IHapticFeedbackManager,
    private val themeRepository: IThemeRepository,
    private val soundManager: ISoundManager,
    private val minefieldRepository: IMinefieldRepository,
    private val analyticsManager: IAnalyticsManager,
    private val playGamesManager: IPlayGamesManager,
    private val clock: Clock,
) : ViewModel() {
    val eventObserver = MutableLiveData<Event>()
    val retryObserver = MutableLiveData<Unit>()
    val shareObserver = MutableLiveData<Unit>()

    private lateinit var gameController: GameController
    private var initialized = false
    private var currentDifficulty: Difficulty = Difficulty.Standard

    val field = MutableLiveData<List<Area>>()
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
            newDifficulty,
            dimensionRepository,
            preferencesRepository
        )

        gameController = GameController(minefield, minefieldRepository.randomSeed())
        initialized = true
        refreshUserPreferences()

        mineCount.postValue(minefield.mines)
        difficulty.postValue(newDifficulty)
        levelSetup.postValue(minefield)
        refreshField()

        eventObserver.postValue(Event.StartNewGame)

        analyticsManager.sentEvent(
            Analytics.NewGame(
                minefield,
                newDifficulty,
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
        levelSetup.postValue(setup)
        refreshField()
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

        val setup = save.minefield
        gameController = GameController(setup, save.seed, save.uid)
        initialized = true
        refreshUserPreferences()

        mineCount.postValue(setup.mines)
        difficulty.postValue(save.difficulty)
        levelSetup.postValue(setup)
        refreshField()

        eventObserver.postValue(Event.ResumeGame)

        analyticsManager.sentEvent(
            Analytics.RetryGame(
                setup,
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

    suspend fun retryGame(uid: Int): Minefield = withContext(Dispatchers.IO) {
        val save = savesRepository.loadFromId(uid)

        if (save != null) {
            saveId.postValue(uid.toLong())
            currentDifficulty = save.difficulty
            retryGame(save)

            withContext(Dispatchers.Main) {
                if (save.firstOpen is FirstOpen.Position) {
                    gameController
                        .singleClick(save.firstOpen.value)
                        .filterNotNull()
                        .collect { refreshField() }
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
            if (gameController.hasMines()) {
                eventObserver.postValue(Event.Pause)
            }
            clock.stop()
        }
    }

    suspend fun saveGame() {
        if (initialized && gameController.hasMines()) {
            val id = savesRepository.saveGame(
                gameController.getSaveState(elapsedTimeSeconds.value ?: 0L, currentDifficulty)
            )
            gameController.setCurrentSaveId(id?.toInt() ?: 0)
            saveId.postValue(id)
        }
    }

    private suspend fun saveStats() {
        if (initialized && gameController.hasMines()) {
            gameController.getStats(elapsedTimeSeconds.value ?: 0L)?.let {
                statsRepository.addStats(it)
            }
        }
    }

    fun resumeGame() {
        if (initialized && gameController.hasMines() && !gameController.isGameOver()) {
            eventObserver.postValue(Event.Resume)
            runClock()
        }
    }

    suspend fun onLongClick(index: Int) {
        gameController
            .longPress(index)
            .filterNotNull()
            .collect { action ->
                onFeedbackAnalytics(action, index)
                refreshField()
                onPostAction()

                if (preferencesRepository.useHapticFeedback()) {
                    hapticFeedbackManager.longPressFeedback()
                }
            }
    }

    suspend fun onDoubleClick(index: Int) {
        gameController
            .doubleClick(index)
            .filterNotNull()
            .collect { action ->
                onFeedbackAnalytics(action, index)
                refreshField()
                onPostAction()
            }
    }

    suspend fun onSingleClick(index: Int) {
        gameController
            .singleClick(index)
            .filterNotNull()
            .collect { action ->
                onFeedbackAnalytics(action, index)
                refreshField()
                onPostAction()
            }
    }

    private fun onPostAction() {
        if (preferencesRepository.useFlagAssistant() && !gameController.hasAnyMineExploded()) {
            gameController.runFlagAssistant()
            refreshField()
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

        if (gameController.hasMines()) {
            refreshMineCount()
        }

        if (gameController.checkVictory()) {
            refreshField()
            eventObserver.postValue(Event.Victory)
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

            showWrongFlags()
            refreshField()

            findExplodedMine()?.let { exploded ->
                takeExplosionRadius(exploded).forEach {
                    revealArea(it.id)
                    refreshField()
                    delay(delayMillis)
                }
            }

            refreshField()
            updateGameState()
        }

        if (!fromResumeGame && currentDifficulty == Difficulty.Standard) {
            preferencesRepository.decrementProgressiveValue()
        }

        viewModelScope.launch {
            saveStats()
            saveGame()

            checkGameOverAchievements()
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
            refreshField()
        }

        if (currentDifficulty == Difficulty.Standard) {
            preferencesRepository.incrementProgressiveValue()
        }

        if (clock.time() < 30) {
            playGamesManager.unlockAchievement(Achievement.ThirtySeconds)
        }

        viewModelScope.launch {
            saveStats()
            saveGame()

            checkVictoryAchievements()
        }
    }

    private suspend fun checkVictoryAchievements() = with(gameController) {
        field.value?.count { it.mark.isFlag() }?.also {
            if (it > 0) {
                playGamesManager.unlockAchievement(Achievement.Flags)
            }
        }

        val time = clock.time()
        if (time > 1L && gameController.getActionsCount() > 7) {
            when (currentDifficulty) {
                Difficulty.Beginner -> {
                    playGamesManager.submitLeaderboard(Leaderboard.BeginnerBestTime, clock.time())
                }
                Difficulty.Intermediate -> {
                    playGamesManager.submitLeaderboard(Leaderboard.IntermediateBestTime, clock.time())
                }
                Difficulty.Expert -> {
                    playGamesManager.submitLeaderboard(Leaderboard.ExpertBestTime, clock.time())
                }
                else -> {
                }
            }

            statsRepository.getAllStats(0).count {
                it.victory == 1
            }.also {
                if (it >= 20) {
                    playGamesManager.incrementAchievement(Achievement.Beginner)
                }

                if (it >= 50) {
                    playGamesManager.incrementAchievement(Achievement.Intermediate)
                }

                if (it >= 100) {
                    playGamesManager.incrementAchievement(Achievement.Expert)
                }
            }
        }
    }

    private fun checkGameOverAchievements() = with(gameController) {
        if (getActionsCount() < 3) {
            playGamesManager.unlockAchievement(Achievement.NoLuck)
        }

        if (almostAchievement()) {
            playGamesManager.unlockAchievement(Achievement.Almost)
        }

        field.value?.count { it.mark.isFlag() }?.also {
            if (it > 0) {
                playGamesManager.incrementAchievement(Achievement.Flags)
            }
        }

        field.value?.count { it.hasMine && it.mistake }?.also {
            if (it > 0) {
                playGamesManager.incrementAchievement(Achievement.Boom)
            }
        }
    }

    fun getAppTheme(): AppTheme = themeRepository.getTheme()

    private fun getAreaSizeMultiplier() = preferencesRepository.areaSizeMultiplier()

    private fun refreshField() {
        field.postValue(gameController.field())

        if (gameController.hasMines()) {
            viewModelScope.launch {
                saveGame()
            }
        }
    }
}
