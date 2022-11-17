package dev.lucasnlm.antimine.common.level.viewmodel

import android.content.Context
import android.text.SpannedString
import android.util.LayoutDirection
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.layoutDirection
import androidx.lifecycle.viewModelScope
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.GameController
import dev.lucasnlm.antimine.common.level.database.models.FirstOpen
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.common.level.repository.ITipRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.core.haptic.HapticFeedbackManager
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.core.sound.ISoundManager
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ActionResponse
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.models.GameControl
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.external.Achievement
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IFeatureFlagManager
import dev.lucasnlm.external.IPlayGamesManager
import dev.lucasnlm.external.Leaderboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

open class GameViewModel(
    private val savesRepository: ISavesRepository,
    private val statsRepository: IStatsRepository,
    private val dimensionRepository: IDimensionRepository,
    private val preferencesRepository: IPreferencesRepository,
    private val hapticFeedbackManager: HapticFeedbackManager,
    private val soundManager: ISoundManager,
    private val minefieldRepository: IMinefieldRepository,
    private val analyticsManager: IAnalyticsManager,
    private val playGamesManager: IPlayGamesManager,
    private val tipRepository: ITipRepository,
    private val featureFlagManager: IFeatureFlagManager,
    private val clock: Clock,
) : IntentViewModel<GameEvent, GameState>() {
    private lateinit var gameController: GameController
    private var initialized = false

    override fun initialState(): GameState {
        return GameState(
            turn = 0,
            field = listOf(),
            duration = 0L,
            difficulty = Difficulty.Beginner,
            mineCount = null,
            minefield = Minefield(9, 9, 9),
            seed = 0L,
            tips = tipRepository.getTotalTips(),
            isGameCompleted = false,
            isActive = false,
            hasMines = false,
            useHelp = preferencesRepository.useHelp(),
            isLoadingMap = true,
            showTutorial = preferencesRepository.showTutorialButton(),
        )
    }

    override suspend fun mapEventToState(event: GameEvent): Flow<GameState> = flow {
        when (event) {
            is GameEvent.SetGameActivation -> {
                val newState = state.copy(isActive = event.active)
                emit(newState)
            }
            is GameEvent.ShowNewGameDialog -> {
                sendSideEffect(GameEvent.ShowNewGameDialog)
            }
            is GameEvent.GiveMoreTip -> {
                tipRepository.increaseTip(event.value)

                val newState = state.copy(
                    tips = tipRepository.getTotalTips(),
                )

                emit(newState)
            }
            is GameEvent.ConsumeTip -> {
                if (tipRepository.removeTip()) {
                    val newState = state.copy(
                        field = gameController.field(),
                        tips = tipRepository.getTotalTips(),
                    )
                    emit(newState)
                }
            }
            is GameEvent.UpdateSave -> {
                val newState = state.copy(saveId = event.saveId)
                emit(newState)
            }
            is GameEvent.NewGame -> {
                emit(event.newState)
            }
            is GameEvent.ContinueGame -> {
                onContinueFromGameOver()
                runClock()
                val newState = state.copy(
                    isActive = true,
                    isGameCompleted = false,
                )
                emit(newState)
            }
            is GameEvent.EngineReady -> {
                emit(state.copy(isLoadingMap = false))

                if (!state.isGameCompleted && state.hasMines && !state.isLoadingMap) {
                    if (
                        !gameController.isGameOver() &&
                        !gameController.isVictory() &&
                        gameController.remainingMines() > 1
                    ) {
                        runClock()
                    }
                } else {
                    stopClock()
                }
            }
            is GameEvent.LoadingNewGame -> {
                stopClock()
                emit(state.copy(isLoadingMap = true, duration = 0L, isActive = false))
            }
            is GameEvent.UpdateTime -> {
                val newState = state.copy(duration = event.time)
                emit(newState)
            }
            is GameEvent.UpdateMinefield -> {
                val isVictory = gameController.isVictory()
                val isGameOver = gameController.isGameOver()
                val isComplete = isCompletedWithMistakes()
                val wasCompleted = state.isGameCompleted
                val hasMines = gameController.hasMines()

                var newState = state.copy(
                    turn = state.turn + 1,
                    field = event.field,
                    mineCount = gameController.remainingMines(),
                    isGameCompleted = isVictory || isGameOver || isComplete,
                    hasMines = hasMines,
                )

                if (!wasCompleted) {
                    when {
                        isVictory && !gameController.hadMistakes() -> {
                            onVictory()
                            newState = newState.copy(field = gameController.field())

                            val totalMines = gameController.mines().count()
                            val sideEffect = GameEvent.VictoryDialog(
                                delayToShow = 0L,
                                totalMines = totalMines,
                                rightMines = totalMines,
                                timestamp = state.duration,
                                receivedTips = calcRewardHints(),
                            )
                            sendSideEffect(sideEffect)
                        }
                        isComplete -> {
                            onGameOver(false)
                            newState = newState.copy(field = gameController.field())
                            val sideEffect = GameEvent.GameCompleteDialog(
                                delayToShow = 0L,
                                totalMines = gameController.mines().count(),
                                rightMines = gameController.mines().count { it.mark.isNotNone() },
                                timestamp = state.duration,
                                receivedTips = calcRewardHints(),
                                turn = state.turn,
                            )
                            sendSideEffect(sideEffect)
                        }
                        isGameOver -> {
                            onGameOver(true)
                            newState = newState.copy(field = gameController.field())
                            val sideEffect = GameEvent.GameOverDialog(
                                delayToShow = explosionDelay(),
                                totalMines = gameController.mines().count(),
                                rightMines = gameController.mines().count { it.mark.isNotNone() },
                                timestamp = state.duration,
                                receivedTips = 0,
                                turn = state.turn,
                            )
                            sendSideEffect(sideEffect)
                        }
                    }
                }

                if (!wasCompleted && hasMines && !newState.isLoadingMap) {
                    runClock()
                } else {
                    stopClock()
                }

                emit(newState)
            }
            else -> {
                // Empty
            }
        }
    }

    suspend fun startNewGame(newDifficulty: Difficulty = state.difficulty): Minefield {
        clock.reset()
        initialized = false

        val minefield = minefieldRepository.fromDifficulty(
            newDifficulty,
            dimensionRepository,
            preferencesRepository,
        )

        withContext(Dispatchers.IO) {
            sendEvent(GameEvent.LoadingNewGame)

            val seed = minefieldRepository.randomSeed()

            gameController = GameController(
                minefield = minefield,
                seed = seed,
                useSimonTatham = preferencesRepository.useSimonTathamAlgorithm(),
                onCreateUnsafeLevel = ::onCreateUnsafeLevel,
                saveId = null,
                difficulty = newDifficulty,
            )

            val newGameState = GameState(
                duration = 0L,
                seed = seed,
                difficulty = newDifficulty,
                minefield = minefield,
                mineCount = minefield.mines,
                field = gameController.field(),
                tips = tipRepository.getTotalTips(),
                isGameCompleted = false,
                isActive = true,
                hasMines = false,
                useHelp = preferencesRepository.useHelp(),
                isLoadingMap = true,
                showTutorial = preferencesRepository.showTutorialButton(),
            )

            sendEvent(GameEvent.NewGame(newGameState))

            initialized = true
            refreshUserPreferences()

            analyticsManager.sentEvent(
                Analytics.NewGame(
                    minefield,
                    newDifficulty,
                    gameController.seed,
                ),
            )
        }

        return minefield
    }

    private fun resumeGameFromSave(save: Save): Minefield {
        clock.reset(save.duration)

        sendEvent(GameEvent.LoadingNewGame)

        gameController = GameController(save, preferencesRepository.useSimonTathamAlgorithm())
        initialized = true

        refreshUserPreferences()

        val newGameState = GameState(
            saveId = save.uid.toLong(),
            duration = save.duration,
            seed = save.seed,
            difficulty = save.difficulty,
            minefield = save.minefield,
            mineCount = gameController.remainingMines(),
            field = gameController.field(),
            tips = tipRepository.getTotalTips(),
            isGameCompleted = isCompletedWithMistakes(),
            isActive = !gameController.allMinesFound(),
            hasMines = true,
            useHelp = preferencesRepository.useHelp(),
            isLoadingMap = true,
            showTutorial = preferencesRepository.showTutorialButton(),
        )

        sendEvent(GameEvent.NewGame(newGameState))

        if (newGameState.isActive && !newGameState.isGameCompleted && !newGameState.isLoadingMap) {
            runClock()
        }

        gameController.increaseErrorToleranceByWrongMines()

        analyticsManager.sentEvent(Analytics.ResumePreviousGame)
        return newGameState.minefield
    }

    private fun retryGame(save: Save) {
        clock.reset()

        sendEvent(GameEvent.LoadingNewGame)

        gameController = GameController(
            minefield = save.minefield,
            seed = save.seed,
            useSimonTatham = preferencesRepository.useSimonTathamAlgorithm(),
            saveId = save.uid,
            onCreateUnsafeLevel = ::onCreateUnsafeLevel,
            difficulty = save.difficulty,
        )
        initialized = true
        refreshUserPreferences()

        val newGameState = GameState(
            saveId = save.uid.toLong(),
            duration = save.duration,
            seed = save.seed,
            difficulty = save.difficulty,
            minefield = save.minefield,
            mineCount = gameController.remainingMines(),
            field = gameController.field(),
            tips = tipRepository.getTotalTips(),
            isGameCompleted = false,
            isActive = true,
            hasMines = false,
            useHelp = preferencesRepository.useHelp(),
            isLoadingMap = true,
            showTutorial = preferencesRepository.showTutorialButton(),
        )

        sendEvent(GameEvent.NewGame(newGameState))

        analyticsManager.sentEvent(
            Analytics.RetryGame(
                newGameState.minefield,
                newGameState.difficulty,
                newGameState.seed,
                save.firstOpen.toInt(),
            ),
        )
    }

    suspend fun loadGame(uid: Int): Minefield = withContext(Dispatchers.IO) {
        val lastGame = savesRepository.loadFromId(uid)

        if (lastGame != null) {
            resumeGameFromSave(lastGame)
        } else {
            // Fail to load
            startNewGame()
        }
    }

    private suspend fun onContinueFromGameOver() {
        if (initialized) {
            gameController.increaseErrorTolerance()
            gameController.dismissMistake()
            statsRepository.deleteLastStats()
            analyticsManager.sentEvent(
                Analytics.ContinueGameAfterGameOver(gameController.getErrorTolerance()),
            )
        }
    }

    private fun isCompletedWithMistakes(): Boolean {
        return gameController.hadMistakes() && gameController.hasIsolatedAllMines()
    }

    suspend fun retryGame(uid: Int): Minefield = withContext(Dispatchers.IO) {
        val save = savesRepository.loadFromId(uid)

        if (save != null) {
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
            resumeGameFromSave(lastGame)
        } else {
            // Fail to load
            startNewGame()
        }
    }

    fun pauseGame() {
        if (initialized) {
            if (gameController.hasMines()) {
                sendEvent(GameEvent.SetGameActivation(false))
            }
            clock.stop()
        }
    }

    suspend fun saveGame() {
        if (initialized) {
            gameController.let {
                if (it.hasMines()) {
                    savesRepository.saveGame(
                        it.getSaveState(state.duration, state.difficulty),
                    )?.let { id ->
                        it.setCurrentSaveId(id.toInt())
                        sendEvent(GameEvent.UpdateSave(id))
                    }
                }
            }
        }
    }

    private suspend fun saveStats() {
        if (initialized) {
            gameController.let {
                if (it.hasMines()) {
                    it.getStats(state.duration)?.let { stats ->
                        statsRepository.addStats(stats)
                    }
                }
            }
        }
    }

    fun resumeGame() {
        if (initialized && gameController.hasMines() && !gameController.isGameOver()) {
            sendEvent(GameEvent.SetGameActivation(true))
        }
    }

    open suspend fun onLongClick(index: Int) {
        gameController
            .longPress(index)
            .filterNotNull()
            .collect { action ->
                onFeedbackAnalytics(action, index)
                onPostAction()
                refreshField()

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
                onPostAction()
                refreshField()
            }
    }

    open suspend fun onSingleClick(index: Int) {
        gameController
            .singleClick(index)
            .filterNotNull()
            .collect { action ->
                onFeedbackAnalytics(action, index)
                onPostAction()
                refreshField()
            }
    }

    private fun onPostAction() {
        if (preferencesRepository.useFlagAssistant() && !gameController.isGameOver()) {
            gameController.runFlagAssistant()
        }

        if (preferencesRepository.dimNumbers() && !gameController.isGameOver()) {
            if (gameController.isVictory()) {
                gameController.runNumberDimmerToAllMines()
            } else {
                gameController.runNumberDimmer()
            }
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
                ActionResponse.OpenNeighbors -> {
                    analyticsManager.sentEvent(Analytics.OpenNeighbors(index))
                }
                ActionResponse.OpenOrMark -> {
                    analyticsManager.sentEvent(Analytics.OpenOrFlagTile(index))
                }
            }
        }
    }

    private fun updateGameState() {
        when {
            gameController.isGameOver() -> {
                sendEvent(GameEvent.SetGameActivation(false))
            }
            else -> {
                sendEvent(GameEvent.SetGameActivation(true))
            }
        }

        if (gameController.isVictory()) {
            sendEvent(GameEvent.SetGameActivation(false))
        }
    }

    private fun refreshUserPreferences() {
        if (initialized) {
            gameController.apply {
                val controlType = preferencesRepository.controlStyle()
                val gameControl = GameControl.fromControlType(controlType)

                updateGameControl(gameControl)
                useQuestionMark(preferencesRepository.useQuestionMark())
                useClickOnNumbers(preferencesRepository.allowTapOnNumbers())
                letNumbersPutFlag(preferencesRepository.letNumbersAutoFlag())
            }
        }
    }

    fun refreshUseOpenOnSwitchControl(useOpen: Boolean) {
        if (initialized) {
            gameController.useOpenOnSwitchControl(useOpen)
        }
    }

    private fun runClock() {
        clock.run {
            if (isStopped) {
                start {
                    sendEvent(GameEvent.UpdateTime(it))
                }
            }
        }
    }

    private fun stopClock() {
        clock.stop()
    }

    private fun showAllEmptyAreas() {
        gameController.revealAllEmptyAreas()
    }

    fun revealRandomMine(consume: Boolean = true): Boolean {
        return if (initialized && gameController.revealRandomMine()) {
            if (consume) {
                sendEvent(GameEvent.ConsumeTip)
            }
            true
        } else {
            false
        }
    }

    private fun explosionDelay() = if (preferencesRepository.useAnimations()) 400L else 0L

    fun hasUnknownMines(): Boolean {
        return !gameController.hasIsolatedAllMines() && gameController.remainingMines() > 1
    }

    fun revealMines() {
        if (initialized) {
            gameController.run {
                showWrongFlags()
                showAllMistakes()
                refreshField()
            }
        }
    }

    private fun getScore() = gameController.getScore()

    private suspend fun onGameOver(useGameOverFeedback: Boolean) {
        stopClock()
        analyticsManager.sentEvent(Analytics.GameOver(clock.time(), getScore()))

        gameController.run {
            if (useGameOverFeedback) {
                if (preferencesRepository.useHapticFeedback()) {
                    hapticFeedbackManager.explosionFeedback()
                }

                if (preferencesRepository.isSoundEffectsEnabled()) {
                    soundManager.play(R.raw.mine_explosion_sound)
                }
            }

            if (hasIsolatedAllMines()) {
                revealAllEmptyAreas()
            }

            refreshField()
            updateGameState()
        }

        if (state.difficulty == Difficulty.Standard || state.difficulty == Difficulty.FixedSize) {
            preferencesRepository.decrementProgressiveValue()
        }

        saveStats()
        saveGame()
        checkGameOverAchievements()
    }

    private fun addNewTip(amount: Int) {
        tipRepository.increaseTip(amount.coerceAtLeast(0))
    }

    fun getTips(): Int {
        return tipRepository.getTotalTips()
    }

    private suspend fun onVictory() {
        analyticsManager.sentEvent(
            Analytics.Victory(
                clock.time(),
                getScore(),
                state.difficulty,
            ),
        )

        stopClock()

        gameController.run {
            showAllEmptyAreas()
            flagAllMines()
            showWrongFlags()
        }

        if (state.difficulty == Difficulty.Standard || state.difficulty == Difficulty.FixedSize) {
            preferencesRepository.incrementProgressiveValue()
        }

        if (clock.time() < 30L) {
            withContext(Dispatchers.Main) {
                playGamesManager.unlockAchievement(Achievement.ThirtySeconds)
            }
        }

        checkVictoryAchievements()
        saveGame()
        saveStats()

        val rewardedHints = calcRewardHints()
        if (rewardedHints > 0) {
            addNewTip(rewardedHints)
        }
    }

    fun isGameStarted(): Boolean {
        return gameController.mines().isNotEmpty()
    }

    private fun calcRewardHints(): Int {
        return if (clock.time() > 10L && preferencesRepository.isPremiumEnabled()) {
            val rewardedHints = if (isCompletedWithMistakes()) {
                (state.minefield.mines * 0.025).toInt()
            } else {
                (state.minefield.mines * 0.05).toInt()
            }

            rewardedHints
        } else {
            0
        }
    }

    private suspend fun checkVictoryAchievements() = with(gameController) {
        state.field.count { it.mark.isFlag() }.also {
            if (it > 0) {
                withContext(Dispatchers.Main) {
                    playGamesManager.incrementAchievement(Achievement.Flags, it)
                }
            }
        }

        val time = clock.time()
        if (time > 1L && gameController.getActionsCount() > 7) {
            val board = when (state.difficulty) {
                Difficulty.Beginner -> {
                    Leaderboard.BeginnerBestTime
                }
                Difficulty.Intermediate -> {
                    Leaderboard.IntermediateBestTime
                }
                Difficulty.Expert -> {
                    Leaderboard.ExpertBestTime
                }
                Difficulty.Master -> {
                    Leaderboard.MasterBestTime
                }
                Difficulty.Legend -> {
                    Leaderboard.LegendaryBestTime
                }
                else -> {
                    null
                }
            }

            board?.let {
                playGamesManager.submitLeaderboard(it, time)
            }

            statsRepository.getAllStats(0).count {
                it.victory == 1
            }.also {
                if (it > 0) {
                    viewModelScope.launch(Dispatchers.Main) {
                        playGamesManager.setAchievementSteps(Achievement.Beginner, it)
                    }

                    viewModelScope.launch(Dispatchers.Main) {
                        playGamesManager.setAchievementSteps(Achievement.Intermediate, it)
                    }

                    viewModelScope.launch(Dispatchers.Main) {
                        playGamesManager.setAchievementSteps(Achievement.Expert, it)
                    }
                }
            }
        }
    }

    private fun checkGameOverAchievements() = with(gameController) {
        viewModelScope.launch {
            if (getActionsCount() < 3) {
                withContext(Dispatchers.Main) {
                    playGamesManager.unlockAchievement(Achievement.NoLuck)
                }
            }

            if (almostAchievement()) {
                withContext(Dispatchers.Main) {
                    playGamesManager.unlockAchievement(Achievement.Almost)
                }
            }

            state.field.count { it.mark.isFlag() }.also {
                if (it > 0) {
                    withContext(Dispatchers.Main) {
                        playGamesManager.incrementAchievement(Achievement.Flags, it)
                    }
                }
            }

            state.field.count { it.hasMine && it.mistake }.also {
                if (it > 0) {
                    withContext(Dispatchers.Main) {
                        playGamesManager.incrementAchievement(Achievement.Boom, it)
                    }
                }
            }
        }
    }

    private fun onCreateUnsafeLevel() {
        sendSideEffect(GameEvent.ShowNoGuessFailWarning)
    }

    fun getControlDescription(context: Context): SpannedString? {
        var openAction: String? = null
        var openReaction: String? = null
        var flagAction: String? = null
        var flagReaction: String? = null
        var result: SpannedString? = null

        when (preferencesRepository.controlStyle()) {
            ControlStyle.Standard -> {
                openAction = context.getString(R.string.single_click)
                openReaction = context.getString(R.string.open)
                flagAction = context.getString(R.string.long_press)
                flagReaction = context.getString(R.string.flag_tile)
            }
            ControlStyle.FastFlag -> {
                openAction = context.getString(R.string.single_click)
                openReaction = context.getString(R.string.flag_tile)
                flagAction = context.getString(R.string.long_press)
                flagReaction = context.getString(R.string.open)
            }
            ControlStyle.DoubleClick -> {
                openAction = context.getString(R.string.single_click)
                openReaction = context.getString(R.string.flag_tile)
                flagAction = context.getString(R.string.double_click)
                flagReaction = context.getString(R.string.open)
            }
            ControlStyle.DoubleClickInverted -> {
                openAction = context.getString(R.string.single_click)
                openReaction = context.getString(R.string.open)
                flagAction = context.getString(R.string.double_click)
                flagReaction = context.getString(R.string.flag_tile)
            }
            else -> {
                // With switch button, it doesn't require toast
            }
        }

        if (openAction != null) {
            val isLTL = Locale.getDefault().layoutDirection == LayoutDirection.LTR

            val first = buildSpannedString {
                if (isLTL) {
                    bold {
                        append(openAction)
                    }
                    append(" - ")
                    append(openReaction)
                } else {
                    bold {
                        append(openReaction)
                    }
                    append(" - ")
                    append(openAction)
                }
            }

            val second = buildSpannedString {
                if (isLTL) {
                    bold {
                        append(flagAction)
                    }
                    append(" - ")
                    append(flagReaction)
                } else {
                    bold {
                        append(flagReaction)
                    }
                    append(" - ")
                    append(flagAction)
                }
            }

            result = buildSpannedString {
                append(first)
                append("\n")
                append(second)
                append("\n")
                append(context.getString(R.string.tap_to_customize))
            }
        }

        return result
    }

    private fun refreshField() {
        sendEvent(GameEvent.UpdateMinefield(gameController.field()))
    }
}
