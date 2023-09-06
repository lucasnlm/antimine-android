package dev.lucasnlm.antimine.common.level.viewmodel

import android.content.Context
import android.text.SpannedString
import android.util.LayoutDirection
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.layoutDirection
import androidx.lifecycle.viewModelScope
import dev.lucasnlm.antimine.common.level.database.models.FirstOpen
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.logic.GameController
import dev.lucasnlm.antimine.common.level.models.ActionCompleted
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.SavesRepository
import dev.lucasnlm.antimine.common.level.repository.StatsRepository
import dev.lucasnlm.antimine.common.level.repository.TipRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.haptic.HapticFeedbackManager
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.models.GameControl
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.external.Achievement
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.FeatureFlagManager
import dev.lucasnlm.external.Leaderboard
import dev.lucasnlm.external.PlayGamesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import dev.lucasnlm.antimine.i18n.R as i18n

open class GameViewModel(
    private val savesRepository: SavesRepository,
    private val statsRepository: StatsRepository,
    private val dimensionRepository: DimensionRepository,
    private val preferencesRepository: PreferencesRepository,
    private val hapticFeedbackManager: HapticFeedbackManager,
    private val soundManager: GameAudioManager,
    private val minefieldRepository: MinefieldRepository,
    private val analyticsManager: AnalyticsManager,
    private val playGamesManager: PlayGamesManager,
    private val tipRepository: TipRepository,
    private val featureFlagManager: FeatureFlagManager,
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
            hints = tipRepository.getTotalTips(),
            isGameCompleted = false,
            isActive = false,
            hasMines = false,
            isCreatingGame = false,
            useHelp = preferencesRepository.useHelp(),
            isLoadingMap = true,
            showTutorial = preferencesRepository.showTutorialButton(),
        )
    }

    override suspend fun mapEventToState(event: GameEvent): Flow<GameState> =
        flow {
            when (event) {
                is GameEvent.CreatingGameEvent -> {
                    val newState = state.copy(isCreatingGame = true)
                    emit(newState)
                }
                is GameEvent.SetGameActivation -> {
                    val newState = state.copy(isActive = event.active)
                    emit(newState)
                }
                is GameEvent.ShowNewGameDialog -> {
                    sendSideEffect(GameEvent.ShowNewGameDialog)
                }
                is GameEvent.GiveMoreTip -> {
                    tipRepository.increaseTip(5)

                    val newState =
                        state.copy(
                            hints = tipRepository.getTotalTips(),
                        )

                    emit(newState)
                }
                is GameEvent.ConsumeTip -> {
                    if (tipRepository.removeTip()) {
                        val newState =
                            state.copy(
                                field = gameController.field(),
                                hints = tipRepository.getTotalTips(),
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
                    val newState =
                        state.copy(
                            isActive = !gameController.allMinesFound(),
                            isGameCompleted = gameController.remainingMines() == 0,
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

                    var newState =
                        state.copy(
                            turn = state.turn + 1,
                            field = event.field,
                            mineCount = gameController.remainingMines(),
                            isGameCompleted = isVictory || isGameOver || isComplete,
                            hasMines = hasMines,
                            isCreatingGame = false,
                        )

                    if (!wasCompleted) {
                        when {
                            isVictory && !gameController.hadMistakes() -> {
                                onVictory()
                                newState = newState.copy(field = gameController.field())

                                val totalMines = gameController.mines().count()
                                val sideEffect =
                                    GameEvent.VictoryDialog(
                                        delayToShow = 1500L,
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
                                val sideEffect =
                                    GameEvent.GameCompleteDialog(
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
                                val sideEffect =
                                    GameEvent.GameOverDialog(
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

        val minefield =
            minefieldRepository.fromDifficulty(
                newDifficulty,
                dimensionRepository,
                preferencesRepository,
            )

        withContext(Dispatchers.IO) {
            sendEvent(GameEvent.LoadingNewGame)

            val seed = minefieldRepository.randomSeed()

            gameController =
                GameController(
                    minefield = minefield,
                    seed = seed,
                    useSimonTatham = preferencesRepository.useSimonTathamAlgorithm(),
                    onCreateUnsafeLevel = ::onCreateUnsafeLevel,
                    saveId = null,
                )

            val newGameState =
                GameState(
                    duration = 0L,
                    seed = seed,
                    difficulty = newDifficulty,
                    minefield = minefield,
                    mineCount = minefield.mines,
                    field = gameController.field(),
                    hints = tipRepository.getTotalTips(),
                    isGameCompleted = false,
                    isActive = true,
                    hasMines = false,
                    isCreatingGame = false,
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

        val newGameState =
            GameState(
                saveId = save.uid.toLong(),
                duration = save.duration,
                seed = save.seed,
                difficulty = save.difficulty,
                minefield = save.minefield,
                mineCount = gameController.remainingMines(),
                field = gameController.field(),
                hints = tipRepository.getTotalTips(),
                isGameCompleted = gameController.remainingMines() == 0,
                isCreatingGame = false,
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

        gameController =
            GameController(
                minefield = save.minefield,
                seed = save.seed,
                useSimonTatham = preferencesRepository.useSimonTathamAlgorithm(),
                saveId = save.uid,
                onCreateUnsafeLevel = ::onCreateUnsafeLevel,
            )
        initialized = true
        refreshUserPreferences()

        val newGameState =
            GameState(
                saveId = save.uid.toLong(),
                duration = save.duration,
                seed = save.seed,
                difficulty = save.difficulty,
                minefield = save.minefield,
                mineCount = gameController.remainingMines(),
                field = gameController.field(),
                hints = tipRepository.getTotalTips(),
                isCreatingGame = false,
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

    suspend fun loadGame(uid: Int): Minefield =
        withContext(Dispatchers.IO) {
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

    suspend fun retryGame(uid: Int): Minefield =
        withContext(Dispatchers.IO) {
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

    suspend fun loadLastGame(): Minefield =
        withContext(Dispatchers.IO) {
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
        if (!gameController.hasMines()) {
            sendEvent(GameEvent.CreatingGameEvent)
        }

        gameController
            .longPress(index)
            .filterNotNull()
            .collect { actionCompleted ->
                onFeedbackAnalytics(actionCompleted.action, index)
                onPostAction()
                playActionSound(actionCompleted)
                refreshField()

                if (preferencesRepository.useHapticFeedback()) {
                    hapticFeedbackManager.longPressFeedback()
                }
            }
    }

    open suspend fun onDoubleClick(index: Int) {
        if (!gameController.hasMines()) {
            sendEvent(GameEvent.CreatingGameEvent)
        }

        gameController
            .doubleClick(index)
            .filterNotNull()
            .collect { actionCompleted ->
                onFeedbackAnalytics(actionCompleted.action, index)
                onPostAction()
                playActionSound(actionCompleted)
                refreshField()
            }
    }

    open suspend fun onSingleClick(index: Int) {
        if (!gameController.hasMines()) {
            sendEvent(GameEvent.CreatingGameEvent)
        }

        gameController
            .singleClick(index)
            .filterNotNull()
            .collect { actionCompleted ->
                onFeedbackAnalytics(actionCompleted.action, index)
                onPostAction()
                playActionSound(actionCompleted)
                refreshField()
            }
    }

    private fun playActionSound(actionCompleted: ActionCompleted) {
        when (actionCompleted.action) {
            Action.OpenTile -> soundManager.playOpenArea()
            Action.OpenOrMark -> {
                if (preferencesRepository.getSwitchControlAction() == Action.OpenTile) {
                    soundManager.playOpenArea()
                } else {
                    soundManager.playPutFlag()
                }
            }
            Action.SwitchMark, Action.QuestionMark -> soundManager.playPutFlag()
            Action.OpenNeighbors -> soundManager.playOpenMultipleArea()
            else -> {
                // No sound
            }
        }
    }

    private fun onPostAction() {
        if (preferencesRepository.useFlagAssistant() && !gameController.isGameOver()) {
            gameController.runFlagAssistant()
        }

        if (preferencesRepository.dimNumbers() && !gameController.isGameOver()) {
            gameController.runNumberDimmer()
        }

        updateGameState()
    }

    private fun onFeedbackAnalytics(
        action: Action,
        index: Int,
    ) {
        if (featureFlagManager.isGameplayAnalyticsEnabled) {
            when (action) {
                Action.OpenTile -> {
                    analyticsManager.sentEvent(Analytics.OpenTile(index))
                }
                Action.SwitchMark -> {
                    analyticsManager.sentEvent(Analytics.SwitchMark(index))
                }
                Action.OpenNeighbors -> {
                    analyticsManager.sentEvent(Analytics.OpenNeighbors(index))
                }
                Action.OpenOrMark -> {
                    analyticsManager.sentEvent(Analytics.OpenOrFlagTile(index))
                }
                Action.QuestionMark -> {
                    analyticsManager.sentEvent(Analytics.QuestionMark(index))
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

    fun changeSwitchControlAction(action: Action) {
        if (initialized) {
            preferencesRepository.setSwitchControl(action)
            gameController.changeSwitchControlAction(action)
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

    fun revealRandomMine(consume: Boolean = true): Int? {
        return if (initialized) {
            val result = gameController.revealRandomMine()

            if (result != null) {
                soundManager.playRevealBomb()

                if (consume) {
                    sendEvent(GameEvent.ConsumeTip)
                }
            }
            result
        } else {
            null
        }
    }

    private fun explosionDelay() = if (preferencesRepository.useAnimations()) EXPLOSION_DELAY else 0L

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

                soundManager.playBombExplosion()
                soundManager.pauseMusic()
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

            if (preferencesRepository.dimNumbers()) {
                runNumberDimmerToAllMines()
            }
        }

        if (state.difficulty == Difficulty.Standard || state.difficulty == Difficulty.FixedSize) {
            preferencesRepository.incrementProgressiveValue()
        }

        if (clock.time() < THIRTY_SECONDS_ACHIEVEMENT) {
            withContext(Dispatchers.Main) {
                playGamesManager.unlockAchievement(Achievement.ThirtySeconds)
            }
        }

        checkVictoryAchievements()
        saveGame()
        saveStats()

        soundManager.playWin()

        val rewardedHints = calcRewardHints()
        if (rewardedHints > 0) {
            addNewTip(rewardedHints)
        }
    }

    private fun calcRewardHints(): Int {
        return if (clock.time() > MIN_REWARD_GAME_SECONDS && preferencesRepository.isPremiumEnabled()) {
            val rewardedHints =
                if (isCompletedWithMistakes()) {
                    (state.minefield.mines * REWARD_RATIO_WITH_MISTAKES)
                } else {
                    (state.minefield.mines * REWARD_RATIO_WITHOUT_MISTAKES)
                }

            rewardedHints.toInt().coerceAtLeast(1)
        } else {
            0
        }
    }

    private suspend fun checkVictoryAchievements() =
        with(gameController) {
            state.field.count { it.mark.isFlag() }.also {
                if (it > 0) {
                    withContext(Dispatchers.Main) {
                        playGamesManager.incrementAchievement(Achievement.Flags, it)
                    }
                }
            }

            val time = clock.time()
            if (time > 1L && gameController.getActionsCount() > MIN_ACTION_TO_REWARD) {
                val board =
                    when (state.difficulty) {
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

    private fun checkGameOverAchievements() =
        with(gameController) {
            viewModelScope.launch {
                if (getActionsCount() < MIN_ACTION_TO_NO_LUCK) {
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
                openAction = context.getString(i18n.string.single_click)
                openReaction = context.getString(i18n.string.open)
                flagAction = context.getString(i18n.string.long_press)
                flagReaction = context.getString(i18n.string.flag_tile)
            }
            ControlStyle.FastFlag -> {
                openAction = context.getString(i18n.string.single_click)
                openReaction = context.getString(i18n.string.flag_tile)
                flagAction = context.getString(i18n.string.long_press)
                flagReaction = context.getString(i18n.string.open)
            }
            ControlStyle.DoubleClick -> {
                openAction = context.getString(i18n.string.single_click)
                openReaction = context.getString(i18n.string.flag_tile)
                flagAction = context.getString(i18n.string.double_click)
                flagReaction = context.getString(i18n.string.open)
            }
            ControlStyle.DoubleClickInverted -> {
                openAction = context.getString(i18n.string.single_click)
                openReaction = context.getString(i18n.string.open)
                flagAction = context.getString(i18n.string.double_click)
                flagReaction = context.getString(i18n.string.flag_tile)
            }
            else -> {
                // With switch button, it doesn't require toast
            }
        }

        if (openAction != null) {
            val isLTL = Locale.getDefault().layoutDirection == LayoutDirection.LTR

            val first =
                buildSpannedString {
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

            val second =
                buildSpannedString {
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

            result =
                buildSpannedString {
                    append(first)
                    appendLine()
                    append(second)
                    appendLine()
                    append(context.getString(i18n.string.tap_to_customize))
                }
        }

        return result
    }

    private fun refreshField() {
        sendEvent(GameEvent.UpdateMinefield(gameController.field()))
    }

    companion object {
        const val EXPLOSION_DELAY = 400L
        const val THIRTY_SECONDS_ACHIEVEMENT = 30L
        const val MIN_REWARD_GAME_SECONDS = 10L
        const val REWARD_RATIO_WITH_MISTAKES = 0.025
        const val REWARD_RATIO_WITHOUT_MISTAKES = 0.05
        const val MIN_ACTION_TO_REWARD = 7
        const val MIN_ACTION_TO_NO_LUCK = 3
    }
}
