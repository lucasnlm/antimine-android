package dev.lucasnlm.antimine.common.level

import dev.lucasnlm.antimine.common.level.database.models.FirstOpen
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.database.models.SaveStatus
import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.common.level.logic.FlagAssistant
import dev.lucasnlm.antimine.common.level.logic.MinefieldCreator
import dev.lucasnlm.antimine.common.level.logic.MinefieldCreatorImpl
import dev.lucasnlm.antimine.common.level.logic.MinefieldCreatorNativeImpl
import dev.lucasnlm.antimine.common.level.logic.MinefieldHandler
import dev.lucasnlm.antimine.common.level.solver.LimitedCheckNeighborsSolver
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.models.Score
import dev.lucasnlm.antimine.preferences.models.ActionResponse
import dev.lucasnlm.antimine.preferences.models.GameControl
import dev.lucasnlm.antimine.preferences.models.Minefield
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class GameController {
    private val minefield: Minefield
    private val startTime = System.currentTimeMillis()
    private var saveId = 0
    private var actions = 0
    private var firstOpen: FirstOpen = FirstOpen.Unknown
    private var gameControl: GameControl = GameControl.Standard
    private var useQuestionMark = true
    private var useOpenOnSwitchControl = true
    private var useNoGuessing = true
    private var useClickOnNumbers = true
    private var errorTolerance = 0
    private var useSimonTatham = true

    val seed: Long

    private val minefieldCreator: MinefieldCreator
    private var field: List<Area>
    private var noGuessTestedLevel = true
    private var onCreateUnsafeLevel: (() -> Unit)? = null

    constructor(
        minefield: Minefield,
        seed: Long,
        useSimonTatham: Boolean,
        saveId: Int? = null,
        difficulty: Difficulty,
        onCreateUnsafeLevel: (() -> Unit)? = null,
    ) {
        val shouldUseSimonTatham = useSimonTatham && difficulty != Difficulty.Beginner
        this.minefieldCreator = if (shouldUseSimonTatham) {
            MinefieldCreatorNativeImpl(minefield, Random(seed))
        } else {
            MinefieldCreatorImpl(minefield, Random(seed))
        }
        this.minefield = minefield
        this.seed = seed
        this.saveId = saveId ?: 0
        this.actions = 0
        this.onCreateUnsafeLevel = onCreateUnsafeLevel
        this.field = minefieldCreator.createEmpty()
        this.useSimonTatham = shouldUseSimonTatham
    }

    constructor(
        save: Save,
        useSimonTatham: Boolean,
    ) {
        this.minefield = save.minefield
        this.seed = save.seed
        this.saveId = save.uid
        this.firstOpen = save.firstOpen
        this.field = save.field
        this.actions = save.actions
        this.minefieldCreator = if (useSimonTatham) {
            MinefieldCreatorNativeImpl(minefield, Random(seed))
        } else {
            MinefieldCreatorImpl(minefield, Random(seed))
        }
    }

    fun field() = field

    fun field(predicate: (Area) -> Boolean) = field.filter(predicate)

    fun mines() = field.filter { it.hasMine }

    fun hasMines() = field.firstOrNull { it.hasMine } != null

    private fun getArea(id: Int) = field.firstOrNull { it.id == id }

    private fun plantMinesExcept(safeId: Int) {
        val solver = LimitedCheckNeighborsSolver()
        do {
            field = minefieldCreator.create(safeId)
            val fieldCopy = field.map { it.copy() }.toMutableList()
            val minefieldHandler = MinefieldHandler(fieldCopy, false)
            minefieldHandler.openAt(safeId, false)
            noGuessTestedLevel = useSimonTatham || solver.trySolve(minefieldHandler.result().toMutableList())
        } while (useNoGuessing && !useSimonTatham && solver.keepTrying() && !noGuessTestedLevel)

        firstOpen = FirstOpen.Position(safeId)
    }

    private fun handleAction(target: Area, actionResponse: ActionResponse?) {
        val mustPlantMines = !hasMines()
        val minefieldHandler: MinefieldHandler

        if (mustPlantMines) {
            plantMinesExcept(target.id)
            minefieldHandler = MinefieldHandler(field.toMutableList(), useQuestionMark)
            minefieldHandler.openAt(target.id, false)

            if (!noGuessTestedLevel) {
                onCreateUnsafeLevel?.invoke()
            }
        } else {
            minefieldHandler = MinefieldHandler(field.toMutableList(), useQuestionMark)
            minefieldHandler.turnOffAllHighlighted()

            when (actionResponse) {
                ActionResponse.OpenTile -> {
                    if (target.mark.isNotNone()) {
                        minefieldHandler.removeMarkAt(target.id)
                    } else {
                        this.actions++
                        minefieldHandler.openAt(target.id, false)
                    }
                }
                ActionResponse.SwitchMark -> {
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
                ActionResponse.HighlightNeighbors -> {
                    if (target.minesAround != 0) {
                        minefieldHandler.highlightAt(target.id)
                    }
                }
                ActionResponse.OpenNeighbors -> {
                    if (useClickOnNumbers) {
                        this.actions++
                        minefieldHandler.openOrFlagNeighborsOf(target.id)
                    }
                }
                ActionResponse.OpenOrMark -> {
                    if (!hasMines()) {
                        if (target.mark.isNotNone()) {
                            minefieldHandler.removeMarkAt(target.id)
                        } else {
                            minefieldHandler.openAt(target.id, false)
                        }
                    } else {
                        this.actions++
                        if (useOpenOnSwitchControl) {
                            if (target.mark.isNone()) {
                                minefieldHandler.openAt(target.id, false)
                            } else {
                                minefieldHandler.removeMarkAt(target.id)
                            }
                        } else {
                            minefieldHandler.switchMarkAt(target.id)
                        }
                    }
                }
            }
        }

        field = minefieldHandler.result()
    }

    fun singleClick(index: Int) = flow {
        getArea(index)?.let { target ->
            val action = if (target.isCovered) {
                gameControl.onCovered.singleClick
            } else {
                gameControl.onUncovered.singleClick
            }
            action?.let {
                handleAction(target, action)
                emit(action)
            }
        }
    }

    fun doubleClick(index: Int) = flow {
        getArea(index)?.let { target ->
            val action = if (target.isCovered) {
                gameControl.onCovered.doubleClick
            } else {
                gameControl.onUncovered.doubleClick
            }
            action?.let {
                handleAction(target, action)
                emit(action)
            }
        }
    }

    fun longPress(index: Int) = flow {
        getArea(index)?.let { target ->
            if (target.isCovered || target.minesAround != 0) {
                val action = if (target.isCovered) {
                    gameControl.onCovered.longPress
                } else {
                    gameControl.onUncovered.longPress
                }
                action?.let {
                    handleAction(target, action)
                    emit(action)
                }
            }
        }
    }

    fun runFlagAssistant() {
        field = FlagAssistant(field.toMutableList()).run {
            runFlagAssistant()
            result()
        }
    }

    fun getScore() = Score(
        mines().count { !it.mistake },
        getMinesCount(),
        field.count()
    )

    fun getMinesCount() = mines().count()

    fun showAllMistakes() {
        field = MinefieldHandler(field.toMutableList(), false).run {
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
        field = MinefieldHandler(field.toMutableList(), false).run {
            flagAllMines()
            result()
        }
    }

    fun showWrongFlags() {
        field = field.map {
            if (!it.hasMine && it.mark.isFlag()) {
                it.copy(mistake = true)
            } else {
                it
            }
        }
    }

    fun revealAllEmptyAreas() {
        field = MinefieldHandler(field.toMutableList(), false).run {
            revealAllEmptyAreas()
            result()
        }
    }

    fun revealRandomMine(): Boolean {
        val result: Boolean
        field = MinefieldHandler(field.toMutableList(), false).run {
            result = revealRandomMineNearUncoveredArea()
            result()
        }
        return result
    }

    fun hasAnyMineExploded(): Boolean = mines().firstOrNull { it.mistake } != null

    private fun explodedMinesCount(): Int = mines().count { it.mistake }

    fun hasFlaggedAllMines(): Boolean = rightFlags() == minefield.mines

    fun hasIsolatedAllMines(): Boolean {
        return field.count { area -> !area.hasMine && area.isCovered } == 0
    }

    fun rightFlags() = mines().count { it.mark.isFlag() }

    fun isVictory(): Boolean =
        hasMines() && hasIsolatedAllMines() && !hasAnyMineExploded()

    fun isGameOver(): Boolean =
        hasIsolatedAllMines() || (explodedMinesCount() > errorTolerance)

    fun remainingMines(): Int {
        val flagsCount = field.count { it.isCovered && it.mark.isFlag() }
        val minesCount = mines().count()
        val openMinesCount = mines().count { !it.isCovered }
        return (minesCount - flagsCount - openMinesCount)
    }

    fun getSaveState(duration: Long, difficulty: Difficulty): Save {
        val saveStatus: SaveStatus = when {
            isVictory() -> SaveStatus.VICTORY
            isGameOver() -> SaveStatus.DEFEAT
            else -> SaveStatus.ON_GOING
        }
        return Save(
            saveId,
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

    fun getErrorTolerance(): Int = errorTolerance

    fun hadMistakes(): Boolean {
        return errorTolerance != 0
    }

    fun getStats(duration: Long): Stats? {
        val gameStatus: SaveStatus = when {
            isVictory() -> SaveStatus.VICTORY
            isGameOver() -> SaveStatus.DEFEAT
            else -> SaveStatus.ON_GOING
        }
        return if (gameStatus == SaveStatus.ON_GOING) {
            null
        } else {
            Stats(
                0,
                duration,
                getMinesCount(),
                if (gameStatus == SaveStatus.VICTORY) 1 else 0,
                minefield.width,
                minefield.height,
                mines().count { !it.isCovered }
            )
        }
    }

    fun setCurrentSaveId(id: Int) {
        this.saveId = id.coerceAtLeast(0)
    }

    fun updateGameControl(newGameControl: GameControl) {
        this.gameControl = newGameControl
    }

    fun useQuestionMark(useQuestionMark: Boolean) {
        this.useQuestionMark = useQuestionMark
    }

    fun useNoGuessing(noGuessing: Boolean) {
        this.useNoGuessing = noGuessing
    }

    fun useClickOnNumbers(clickNumbers: Boolean) {
        this.useClickOnNumbers = clickNumbers
    }

    fun useOpenOnSwitchControl(useOpen: Boolean) {
        this.useOpenOnSwitchControl = useOpen
    }
}
