package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import kotlin.random.Random

interface IMinefieldRepository {
    fun baseStandardSize(
        dimensionRepository: IDimensionRepository,
        progressiveMines: Int,
    ): Minefield

    fun fromDifficulty(
        difficulty: Difficulty,
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository,
    ): Minefield

    fun randomSeed(): Long
}

class MinefieldRepository : IMinefieldRepository {
    override fun baseStandardSize(
        dimensionRepository: IDimensionRepository,
        progressiveMines: Int,
    ): Minefield {
        val fieldSize = dimensionRepository.defaultAreaSize()
        val verticalGap = if (dimensionRepository.navigationBarHeight() > 0)
            VERTICAL_STANDARD_GAP else VERTICAL_STANDARD_GAP_WITHOUT_BOTTOM

        val display = dimensionRepository.displaySize()
        val width = display.width
        val height = display.height

        val calculatedWidth = ((width / fieldSize).toInt() - HORIZONTAL_STANDARD_GAP)
        val calculatedHeight = ((height / fieldSize).toInt() - verticalGap)
        val fitWidth = calculatedWidth.coerceAtLeast(MIN_STANDARD_WIDTH)
        val fitHeight = calculatedHeight.coerceAtLeast(MIN_STANDARD_HEIGHT)
        val fieldArea = fitWidth * fitHeight
        val fitMines =
            ((fieldArea * CUSTOM_LEVEL_MINE_RATIO).toInt() + progressiveMines)
                .coerceAtMost((fieldArea * MAX_LEVEL_MINE_RATIO).toInt())
        return Minefield(fitWidth, fitHeight, fitMines)
    }

    override fun fromDifficulty(
        difficulty: Difficulty,
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository,
    ): Minefield =
        when (difficulty) {
            Difficulty.Standard -> calculateStandardMode(dimensionRepository, preferencesRepository)
            Difficulty.Beginner -> beginnerMinefield
            Difficulty.Intermediate -> intermediateMinefield
            Difficulty.Expert -> expertMinefield
            Difficulty.Master -> masterMinefield
            Difficulty.Legend -> legendMinefield
            Difficulty.FixedSize -> baseStandardSize(dimensionRepository, 0)
            Difficulty.Custom -> preferencesRepository.customGameMode()
        }

    private fun calculateStandardMode(
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository,
    ): Minefield {
        var result: Minefield = baseStandardSize(dimensionRepository, preferencesRepository.getProgressiveValue())
        var resultWidth = result.width
        var resultHeight = result.height

        do {
            val percentage = (result.mines.toDouble() / (resultWidth * resultHeight) * 100.0).toInt()
            result = Minefield(resultWidth, resultHeight, result.mines)
            if (percentage <= 22) {
                break
            } else {
                resultWidth += 2
                resultHeight += 2
            }
        } while (percentage > 22)

        return result
    }

    override fun randomSeed(): Long = Random.nextLong()

    companion object {
        private val beginnerMinefield = Minefield(9, 9, 10)
        private val intermediateMinefield = Minefield(16, 16, 40)
        private val expertMinefield = Minefield(24, 24, 99)
        private val masterMinefield = Minefield(50, 50, 400)
        private val legendMinefield = Minefield(100, 100, 2000)

        private const val CUSTOM_LEVEL_MINE_RATIO = 0.2
        private const val MAX_LEVEL_MINE_RATIO = 0.45
        private const val HORIZONTAL_STANDARD_GAP = 1
        private const val VERTICAL_STANDARD_GAP_WITHOUT_BOTTOM = 4
        private const val VERTICAL_STANDARD_GAP = 3
        private const val MIN_STANDARD_WIDTH = 6
        private const val MIN_STANDARD_HEIGHT = 9
    }
}
