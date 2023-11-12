package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.io.models.FirstOpen
import dev.lucasnlm.antimine.common.io.models.Save
import dev.lucasnlm.antimine.common.io.models.SaveStatus
import dev.lucasnlm.antimine.common.io.models.Stats
import dev.lucasnlm.antimine.common.level.models.ActionCompleted
import dev.lucasnlm.antimine.common.level.solver.LimitedCheckNeighborsSolver
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.models.Mark
import dev.lucasnlm.antimine.core.models.Score
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.GameControl
import dev.lucasnlm.antimine.preferences.models.Minefield
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

class GameController {
    private val minefield: Minefield
    private val startTime = System.currentTimeMillis()
    private var saveId: String? = null
    private var actions = 0
    private var firstOpen: FirstOpen = FirstOpen.Unknown
    private var gameControl: GameControl = GameControl.Standard
    private var useQuestionMark = true
    private var selectedAction: Action
    private var useClickOnNumbers = true
    private var letNumbersPutFlag = true
    private var errorTolerance = 0
    private var useSimonTatham = true
    private var creatingMinefield = false

    private var lastIdInteractionX: Int? = null
    private var lastIdInteractionY: Int? = null

    val seed: Long

    private val minefieldCreator: MinefieldCreator
    private val fallbackCreator: MinefieldCreator
    private var field: List<Area>
    private var noGuessTestedLevel = true
    private var onCreateUnsafeLevel: (() -> Unit)? = null

    constructor(
        minefield: Minefield,
        seed: Long,
        useSimonTatham: Boolean,
        selectedAction: Action,
        saveId: String? = null,
        onCreateUnsafeLevel: (() -> Unit)? = null,
    ) {
        val creationSeed = minefield.seed ?: seed
        val shouldUseSimonTatham = useSimonTatham
        this.fallbackCreator = MinefieldCreatorImpl(minefield, creationSeed)
        this.minefieldCreator =
            if (shouldUseSimonTatham) {
                MinefieldCreatorNativeImpl(minefield, creationSeed)
            } else {
                fallbackCreator
            }
        this.minefield = minefield
        this.seed = seed
        this.saveId = saveId
        this.actions = 0
        this.onCreateUnsafeLevel = onCreateUnsafeLevel
        this.field = minefieldCreator.createEmpty()
        this.useSimonTatham = shouldUseSimonTatham
        this.selectedAction = selectedAction
    }

    constructor(
        save: Save,
        useSimonTatham: Boolean,
        selectedAction: Action,
    ) {
        this.minefield = save.minefield
        this.seed = save.seed
        this.saveId = save.id
        this.firstOpen = save.firstOpen
        this.field = save.field
        this.actions = save.actions
        this.selectedAction = selectedAction
        this.fallbackCreator = MinefieldCreatorImpl(minefield, seed)
        this.minefieldCreator =
            if (useSimonTatham) {
                MinefieldCreatorNativeImpl(minefield, seed)
            } else {
                fallbackCreator
            }
    }

    fun field() = field

    fun field(predicate: (Area) -> Boolean) = field.filter(predicate)

    fun mines() = field.filter { it.hasMine }

    fun hasMines() = field.firstOrNull { it.hasMine } != null

    private fun useIndividualActions(): Boolean = gameControl == GameControl.SwitchMarkOpen

    private fun getArea(id: Int) = field.firstOrNull { it.id == id }

    private suspend fun plantMinesExcept(safeId: Int): Boolean {
        if (!creatingMinefield) {
            val solver = LimitedCheckNeighborsSolver()
            creatingMinefield = true

            runCatching {
                // Try using native implementation first.
                // If it fails, use fallback random generator.
                withTimeout(MAX_CREATION_TIME_MS) {
                    field = minefieldCreator.create(safeId)
                    val fieldCopy = field.map { it.copy() }.toMutableList()
                    val minefieldHandler =
                        MinefieldHandler(
                            field = fieldCopy,
                            useQuestionMark = false,
                            individualActions = useIndividualActions(),
                        )
                    minefieldHandler.openAt(safeId, false)
                }
            }.onFailure {
                do {
                    var solvable: Boolean
                    field = fallbackCreator.create(safeId)
                    val fieldCopy = field.map { it.copy() }.toMutableList()
                    val minefieldHandler =
                        MinefieldHandler(
                            field = fieldCopy,
                            useQuestionMark = false,
                            individualActions = useIndividualActions(),
                        )
                    minefieldHandler.openAt(safeId, false)
                    solvable = solver.trySolve(minefieldHandler.result().toMutableList())
                    noGuessTestedLevel = solvable
                } while (solver.keepTrying() && !noGuessTestedLevel)
            }

            firstOpen = FirstOpen.Position(safeId)
            creatingMinefield = false
            return true
        } else {
            return false
        }
    }

    private suspend fun handleAction(
        target: Area,
        action: Action?,
    ) {
        if (creatingMinefield) {
            // Ignore because the game is not ready for any action.
            return
        }

        val mustPlantMines = !hasMines()
        var minefieldHandler: MinefieldHandler? = null

        if (mustPlantMines) {
            val created = plantMinesExcept(target.id)
            if (created) {
                minefieldHandler =
                    MinefieldHandler(
                        field = field.toMutableList(),
                        useQuestionMark = useQuestionMark,
                        individualActions = useIndividualActions(),
                    )
                minefieldHandler.openAt(target.id, false)

                if (!noGuessTestedLevel) {
                    onCreateUnsafeLevel?.invoke()
                }
            }
        } else {
            minefieldHandler =
                MinefieldHandler(
                    field = field.toMutableList(),
                    useQuestionMark = useQuestionMark,
                    individualActions = useIndividualActions(),
                )

            when (action) {
                Action.OpenTile -> {
                    if (target.mark.isNotNone()) {
                        minefieldHandler.removeMarkAt(target.id)
                    } else {
                        this.actions++
                        minefieldHandler.openAt(target.id, false)
                    }
                }
                Action.SwitchMark -> {
                    if (!hasMines()) {
                        if (target.mark.isNotNone()) {
                            minefieldHandler.removeMarkAt(target.id)
                        } else {
                            minefieldHandler.openAt(target.id, false)
                        }
                    } else {
                        minefieldHandler.switchMarkAt(target.id)
                    }
                }
                Action.OpenNeighbors -> {
                    if (useClickOnNumbers) {
                        this.actions++
                        if (letNumbersPutFlag) {
                            minefieldHandler.openOrFlagNeighborsOf(target.id)
                        } else {
                            minefieldHandler.openNeighborsOf(target.id)
                        }
                    }
                }
                Action.OpenOrMark -> {
                    if (!hasMines()) {
                        if (target.mark.isNotNone()) {
                            minefieldHandler.removeMarkAt(target.id)
                        } else {
                            minefieldHandler.openAt(target.id, false)
                        }
                    } else {
                        this.actions++

                        when (selectedAction) {
                            Action.OpenTile -> {
                                if (target.mark.isNone()) {
                                    minefieldHandler.openAt(target.id, false)
                                } else {
                                    minefieldHandler.removeMarkAt(target.id)
                                }
                            }
                            Action.SwitchMark -> {
                                minefieldHandler.switchMarkAt(target.id)
                            }
                            Action.QuestionMark -> {
                                minefieldHandler.toggleMarkAt(target.id, Mark.Question)
                            }
                            else -> {
                                // Unexpected Action. Ignore.
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        lastIdInteractionX = target.posX
        lastIdInteractionY = target.posY

        minefieldHandler?.let {
            field = it.result()
        }
    }

    fun singleClick(index: Int): Flow<ActionCompleted> =
        flow {
            if (!creatingMinefield) {
                getArea(index)?.let { target ->
                    val action =
                        if (target.isCovered) {
                            gameControl.onCovered.singleClick
                        } else {
                            gameControl.onUncovered.singleClick
                        }
                    action?.let {
                        val initActions = actions
                        handleAction(target, action)
                        emit(
                            ActionCompleted(action, actions - initActions),
                        )
                    }
                }
            }
        }

    fun doubleClick(index: Int): Flow<ActionCompleted> =
        flow {
            if (!creatingMinefield) {
                getArea(index)?.let { target ->
                    val action =
                        if (target.isCovered) {
                            gameControl.onCovered.doubleClick
                        } else {
                            gameControl.onUncovered.doubleClick
                        }
                    action?.let {
                        val initActions = actions
                        handleAction(target, action)
                        emit(
                            ActionCompleted(action, actions - initActions),
                        )
                    }
                }
            }
        }

    fun longPress(index: Int): Flow<ActionCompleted> =
        flow {
            if (!creatingMinefield) {
                getArea(index)?.let { target ->
                    if (target.isCovered || target.minesAround != 0) {
                        val action =
                            if (target.isCovered) {
                                gameControl.onCovered.longPress
                            } else {
                                gameControl.onUncovered.longPress
                            }
                        action?.let {
                            val initActions = actions
                            handleAction(target, action)
                            emit(
                                ActionCompleted(action, actions - initActions),
                            )
                        }
                    }
                }
            }
        }

    fun runFlagAssistant() {
        field =
            FlagAssistant(field.toMutableList()).run {
                runFlagAssistant()
                result()
            }
    }

    fun runNumberDimmer() {
        field =
            NumberDimmer(field.toMutableList()).run {
                runDimmer()
                result()
            }
    }

    fun runNumberDimmerToAllMines() {
        field =
            NumberDimmer(field.toMutableList()).run {
                runDimmerAll()
                result()
            }
    }

    fun getScore() =
        Score(
            mines().count { !it.mistake },
            getMinesCount(),
            field.count(),
        )

    fun getMinesCount() = mines().count()

    fun showAllMistakes() {
        field =
            MinefieldHandler(
                field = field.toMutableList(),
                useQuestionMark = false,
                individualActions = useIndividualActions(),
            ).run {
                showAllMines()
                showAllWrongFlags()
                result()
            }
    }

    fun findExplodedMine() = mines().firstOrNull { it.mistake }

    fun takeExplosionRadius(target: Area): List<Area> =
        mines().filter { it.isCovered && it.mark.isNone() }.sortedBy {
            val dx1 = (it.posX - target.posX)
            val dy1 = (it.posY - target.posY)
            dx1 * dx1 + dy1 * dy1
        }

    fun flagAllMines() {
        field =
            MinefieldHandler(
                field = field.toMutableList(),
                useQuestionMark = false,
                individualActions = useIndividualActions(),
            ).run {
                flagAllMines()
                result()
            }
    }

    fun showWrongFlags() {
        field =
            field.map {
                if (!it.hasMine && it.mark.isFlag()) {
                    it.copy(mistake = true)
                } else {
                    it
                }
            }
    }

    fun revealAllEmptyAreas() {
        field =
            MinefieldHandler(
                field = field.toMutableList(),
                useQuestionMark = false,
                individualActions = useIndividualActions(),
            ).run {
                revealAllEmptyAreas()
                result()
            }
    }

    /**
     * Reveal a random mine near an uncovered area.
     *
     * @param visibleMines It will prioritize mines that are in the visible area.
     * @return The id of the revealed mine.
     */
    fun revealRandomMine(visibleMines: Set<Int>): Int? {
        val resultId: Int?
        field =
            MinefieldHandler(
                field = field.toMutableList(),
                useQuestionMark = false,
                individualActions = useIndividualActions(),
            ).run {
                resultId =
                    revealRandomMineNearUncoveredArea(
                        visibleMines = visibleMines,
                        lastX = lastIdInteractionX,
                        lastY = lastIdInteractionY,
                    )
                result()
            }
        return resultId
    }

    fun hasAnyMineExploded(): Boolean = mines().firstOrNull { it.mistake } != null

    private fun explodedMinesCount(): Int = mines().count { !it.isCovered && it.hasMine }

    fun hasFlaggedAllMines(): Boolean = rightFlags() == minefield.mines

    fun hasIsolatedAllMines(): Boolean {
        return field.count { area -> !area.hasMine && area.isCovered } == 0
    }

    fun rightFlags() = mines().count { it.mark.isFlag() }

    fun isVictory(): Boolean = hasMines() && hasIsolatedAllMines() && !hasAnyMineExploded()

    fun isGameOver(): Boolean = hasIsolatedAllMines() || (explodedMinesCount() > errorTolerance)

    fun allMinesFound(): Boolean {
        return mines().count { !it.isCovered || it.mark.isNotNone() } == mines().count()
    }

    fun remainingMines(): Int {
        val flagsCount = field.count { it.isCovered && it.mark.isFlag() }
        val minesCount = mines().count()
        val openMinesCount = mines().count { !it.isCovered }
        return (minesCount - flagsCount - openMinesCount)
    }

    fun getSaveState(
        duration: Long,
        difficulty: Difficulty,
    ): Save {
        val saveStatus: SaveStatus =
            when {
                isVictory() -> SaveStatus.VICTORY
                isGameOver() -> SaveStatus.DEFEAT
                else -> SaveStatus.ON_GOING
            }
        return Save(
            id = saveId,
            seed = seed,
            startDate = startTime,
            duration = duration,
            minefield = minefield,
            difficulty = difficulty,
            firstOpen = firstOpen,
            status = saveStatus,
            field = field.toList(),
            actions = actions,
        )
    }

    fun almostAchievement(): Boolean {
        return mines().count() - mines().count { it.isCovered && it.mark.isFlag() } == 1
    }

    fun getActionsCount() = actions

    fun increaseErrorToleranceByWrongMines() {
        val value = mines().count { !it.isCovered }
        increaseErrorTolerance(value)
    }

    fun increaseErrorTolerance(value: Int = 1) {
        errorTolerance += value
    }

    fun dismissMistake() {
        val minefieldHandler =
            MinefieldHandler(
                field = field.toMutableList(),
                useQuestionMark = useQuestionMark,
                individualActions = useIndividualActions(),
            )
        minefieldHandler.dismissMistake()
        field = minefieldHandler.result()
    }

    fun getErrorTolerance(): Int = errorTolerance

    fun hadMistakes(): Boolean {
        return errorTolerance != 0
    }

    fun getStats(duration: Long): Stats? {
        val gameStatus: SaveStatus =
            when {
                isVictory() -> SaveStatus.VICTORY
                isGameOver() -> SaveStatus.DEFEAT
                else -> SaveStatus.ON_GOING
            }
        return if (gameStatus == SaveStatus.ON_GOING) {
            null
        } else {
            Stats(
                duration,
                getMinesCount(),
                if (gameStatus == SaveStatus.VICTORY) 1 else 0,
                minefield.width,
                minefield.height,
                mines().count { !it.isCovered },
            )
        }
    }

    fun setCurrentSaveId(saveId: String?) {
        this.saveId = saveId
    }

    fun updateGameControl(newGameControl: GameControl) {
        this.gameControl = newGameControl
    }

    fun useQuestionMark(useQuestionMark: Boolean) {
        this.useQuestionMark = useQuestionMark
    }

    fun getSelectedAction() = selectedAction

    fun useClickOnNumbers(clickNumbers: Boolean) {
        this.useClickOnNumbers = clickNumbers
    }

    fun changeSwitchControlAction(action: Action) {
        this.selectedAction = action
    }

    fun letNumbersPutFlag(enabled: Boolean) {
        this.letNumbersPutFlag = enabled
    }

    private companion object {
        const val MAX_CREATION_TIME_MS = 30000L
    }
}
