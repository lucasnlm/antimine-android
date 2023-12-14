package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import java.lang.Integer.min

class MinefieldRepositoryImpl : MinefieldRepository {
    override fun baseStandardSize(
        dimensionRepository: DimensionRepository,
        progressiveMines: Int,
        limitToMax: Boolean,
    ): Minefield {
        val fieldSize = dimensionRepository.areaSize()
        val horizontalGap =
            if (dimensionRepository.horizontalNavigationBarHeight() > 0) {
                HORIZONTAL_STANDARD_GAP
            } else {
                HORIZONTAL_STANDARD_GAP_WITHOUT_SIDE
            }
        val verticalGap =
            if (dimensionRepository.verticalNavigationBarHeight() > 0) {
                VERTICAL_STANDARD_GAP
            } else {
                VERTICAL_STANDARD_GAP_WITHOUT_BOTTOM
            }

        val display = dimensionRepository.displayMetrics()
        val width = display.widthPixels
        val height = display.heightPixels

        val calculatedWidth = ((width / fieldSize).toInt() - horizontalGap)
        val calculatedHeight = ((height / fieldSize).toInt() - verticalGap)
        val fitWidth = calculatedWidth.coerceAtLeast(MIN_STANDARD_WIDTH)
        val fitHeight = calculatedHeight.coerceAtLeast(MIN_STANDARD_HEIGHT)
        val fieldArea = fitWidth * fitHeight
        val maxMines =
            if (limitToMax) {
                (fieldArea * 0.75).toInt()
            } else {
                Int.MAX_VALUE
            }
        val fitMines = min(maxMines, ((fieldArea * CUSTOM_LEVEL_MINE_RATIO).toInt() + progressiveMines))
        return Minefield(fitWidth, fitHeight, fitMines)
    }

    override fun fromDifficulty(
        difficulty: Difficulty,
        dimensionRepository: DimensionRepository,
        preferencesRepository: PreferencesRepository,
    ): Minefield =
        when (difficulty) {
            Difficulty.Standard -> calculateStandardMode(dimensionRepository, preferencesRepository)
            Difficulty.Beginner -> beginnerMinefield
            Difficulty.Intermediate -> intermediateMinefield
            Difficulty.Expert -> expertMinefield
            Difficulty.Master -> masterMinefield
            Difficulty.Legend -> legendMinefield
            Difficulty.FixedSize ->
                baseStandardSize(
                    dimensionRepository = dimensionRepository,
                    progressiveMines = preferencesRepository.getProgressiveValue(),
                    limitToMax = true,
                )
            Difficulty.Custom -> preferencesRepository.customGameMode()
        }

    private fun calculateStandardMode(
        dimensionRepository: DimensionRepository,
        preferencesRepository: PreferencesRepository,
    ): Minefield {
        var result: Minefield =
            baseStandardSize(
                dimensionRepository = dimensionRepository,
                progressiveMines = preferencesRepository.getProgressiveValue(),
                limitToMax = false,
            )
        var resultWidth = result.width
        var resultHeight = result.height

        do {
            val percentage = result.mines.toDouble() / (resultWidth * resultHeight)
            result = Minefield(resultWidth, resultHeight, result.mines)
            if (percentage <= MAX_MINE_RATIO) {
                break
            } else {
                resultWidth += 2
                resultHeight += 2
            }
        } while (true)

        return result
    }

    override fun randomSeed(): Long = System.currentTimeMillis()

    companion object {
        private val beginnerMinefield = Minefield(9, 9, 10)
        private val intermediateMinefield = Minefield(16, 16, 40)
        private val expertMinefield = Minefield(24, 24, 99)
        private val masterMinefield = Minefield(50, 50, 450)
        private val legendMinefield = Minefield(100, 100, 2000)

        private const val CUSTOM_LEVEL_MINE_RATIO = 0.18
        private const val HORIZONTAL_STANDARD_GAP_WITHOUT_SIDE = 1
        private const val HORIZONTAL_STANDARD_GAP = 3
        private const val VERTICAL_STANDARD_GAP_WITHOUT_BOTTOM = 4
        private const val VERTICAL_STANDARD_GAP = 3
        private const val MIN_STANDARD_WIDTH = 6
        private const val MIN_STANDARD_HEIGHT = 9
        private const val MAX_MINE_RATIO = 0.22
    }
}
