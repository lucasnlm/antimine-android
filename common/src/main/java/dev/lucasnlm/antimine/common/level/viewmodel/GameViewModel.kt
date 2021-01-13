package dev.lucasnlm.antimine.common.level.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.GameController
import dev.lucasnlm.antimine.common.level.database.models.FirstOpen
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.common.level.repository.ITipRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackManager
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.sound.ISoundManager
import dev.lucasnlm.antimine.preferences.models.ActionResponse
import dev.lucasnlm.antimine.preferences.models.GameControl
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.external.Achievement
import dev.lucasnlm.external.IFeatureFlagManager
import dev.lucasnlm.external.IPlayGamesManager
import dev.lucasnlm.external.Leaderboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class GameViewModel(
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
    private val tipRepository: ITipRepository,
    private val featureFlagManager: IFeatureFlagManager,
    private val clock: Clock,
) : ViewModel() {
    val eventObserver = MutableLiveData<Event>()
    val retryObserver = MutableLiveData<Unit>()
    val continueObserver = MutableLiveData<Unit>()
    val shareObserver = MutableLiveData<Unit>()
    val showNewGame = MutableLiveData<Unit>()

    private lateinit var gameController: GameController
    private var initialized = false
    private var currentDifficulty: Difficulty = Difficulty.Standard

    val field = MutableLiveData<List<Area>>()
    val elapsedTimeSeconds = MutableLiveData<Long>()
    val mineCount = MutableLiveData<Int>()
    val difficulty = MutableLiveData<Difficulty>()
    val levelSetup = MutableLiveData<Minefield>()
    val saveId = MutableLiveData<Long>()
    val tips = MutableLiveData(tipRepository.getTotalTips())

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
            gameController.isGameOver() -> eventObserver.postValue(Event.GameOver)
            gameController.isVictory() -> eventObserver.postValue(Event.Victory)
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

    fun increaseErrorTolerance() {
        gameController.increaseErrorTolerance()
    }

    fun isCompletedWithMistakes(): Boolean {
        return gameController.hadMistakes() && gameController.hasIsolatedAllMines()
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

    suspend fun loadGame(): Minefield {
        val currentLevelSetup = levelSetup.value
        return currentLevelSetup ?: loadLastGame()
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
        if (initialized) {
            gameController.let {
                if (it.hasMines()) {
                    val id = savesRepository.saveGame(
                        it.getSaveState(elapsedTimeSeconds.value ?: 0L, currentDifficulty)
                    )
                    it.setCurrentSaveId(id?.toInt() ?: 0)
                    saveId.postValue(id)
                }
            }
        }
    }

    suspend fun saveStats() {
        if (initialized) {
            gameController.let {
                if (it.hasMines()) {
                    it.getStats(elapsedTimeSeconds.value ?: 0L)?.let { stats ->
                        statsRepository.addStats(stats)
                    }
                }
            }
        }
    }

    fun resumeGame() {
        if (initialized && gameController.hasMines() && !gameController.isGameOver()) {
            eventObserver.postValue(Event.Resume)
            runClock()
        }
    }

    open suspend fun onLongClick(index: Int) {
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

    open suspend fun onDoubleClick(index: Int) {
        gameController
            .doubleClick(index)
            .filterNotNull()
            .collect { action ->
                onFeedbackAnalytics(action, index)
                refreshField()
                onPostAction()
            }
    }

    open suspend fun onSingleClick(index: Int) {
        gameController
            .singleClick(index)
            .filterNotNull()
            .collect { action ->
                onFeedbackAnalytics(action, index)
                refreshField()
                onPostAction()
            }
    }

    open suspend fun onInteractOnDisabled() {
        showNewGame.postValue(Unit)
    }

    private fun onPostAction() {
        if (preferencesRepository.useFlagAssistant() && !gameController.isGameOver()) {
            gameController.runFlagAssistant()
            refreshField()
        }

        updateGameState()
    }

    private fun onFeedbackAnalytics(action: ActionResponse, index: Int) {
        if (featureFlagManager.isGameplayAnalyticsEnabled) {
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
                ActionResponse.OpenOrMark -> {
                    analyticsManager.sentEvent(Analytics.OpenOrFlagTile(index))
                }
            }
        }
    }

    private fun refreshMineCount() = mineCount.postValue(gameController.remainingMines())

    private fun updateGameState() {
        when {
            gameController.isGameOver() -> {
                eventObserver.postValue(Event.GameOver)
            }
            else -> {
                eventObserver.postValue(Event.Running)
            }
        }

        if (gameController.hasMines()) {
            refreshMineCount()
        }

        if (gameController.isVictory()) {
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
                useNoGuessing(preferencesRepository.useNoGuessingAlgorithm())
            }
        }
    }

    fun refreshUseOpenOnSwitchControl(useOpen: Boolean) {
        if (initialized) {
            gameController.useOpenOnSwitchControl(useOpen)
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

    fun showAllEmptyAreas() {
        gameController.revealAllEmptyAreas()
    }

    fun hasPlantedMines(): Boolean {
        return gameController.mines().isNotEmpty()
    }

    fun revealRandomMine(): Boolean {
        return if (gameController.revealRandomMine()) {
            if (tipRepository.removeTip()) {
                refreshField()
            }

            tips.postValue(tipRepository.getTotalTips())
            true
        } else {
            false
        }
    }

    fun explosionDelay() = if (preferencesRepository.useAnimations()) 750L else 0L

    fun hasUnknownMines(): Boolean {
        return !gameController.hasIsolatedAllMines()
    }

    suspend fun revealMines() {
        val explosionTime = (explosionDelay() / gameController.getMinesCount().coerceAtLeast(10))
        val delayMillis = explosionTime.coerceAtMost(25L)

        gameController.run {
            showWrongFlags()
            refreshField()

            findExplodedMine()?.let { exploded ->
                takeExplosionRadius(exploded).take(20).forEach {
                    revealArea(it.id)
                    refreshField()
                    delay(delayMillis)
                }
            }

            showAllMines()
            refreshField()
        }
    }

    fun flagAllMines() {
        gameController.run {
            flagAllMines()
            refreshField()
        }
    }

    suspend fun gameOver(fromResumeGame: Boolean, useGameOverFeedback: Boolean) {
        gameController.run {
            analyticsManager.sentEvent(Analytics.GameOver(clock.time(), getScore()))

            if (!fromResumeGame && useGameOverFeedback) {
                if (preferencesRepository.useHapticFeedback()) {
                    hapticFeedbackManager.explosionFeedback()
                }

                if (preferencesRepository.isSoundEffectsEnabled()) {
                    soundManager.play(R.raw.mine_explosion_sound)
                }
            }

            if (gameController.hasIsolatedAllMines()) {
                gameController.revealAllEmptyAreas()
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

    fun addNewTip() {
        tipRepository.increaseTip()
    }

    fun getTips(): Int {
        return tipRepository.getTotalTips()
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

        if (clock.time() < 30L) {
            playGamesManager.unlockAchievement(Achievement.ThirtySeconds)
        }

        viewModelScope.launch {
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

    private fun getAreaSizeMultiplier() = preferencesRepository.squareSizeMultiplier()

    private fun refreshField() {
        field.postValue(gameController.field())
    }
}
