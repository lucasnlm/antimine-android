package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import kotlin.random.Random

interface IMinefieldRepository {
    fun fromDifficulty(
        difficulty: Difficulty,
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository,
    ): Minefield

    fun randomSeed(): Long
}

class MinefieldRepository : IMinefieldRepository {
    override fun fromDifficulty(
        difficulty: Difficulty,
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository,
    ): Minefield =
        when (difficulty) {
            Difficulty.Standard -> calculateStandardMode(
                dimensionRepository,
                preferencesRepository
            )
            Difficulty.Beginner -> beginnerMinefield
            Difficulty.Intermediate -> intermediateMinefield
            Difficulty.Expert -> expertMinefield
            Difficulty.Custom -> preferencesRepository.customGameMode()
        }

    private fun calculateStandardMode(
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository,
    ): Minefield {
        val fieldSize = dimensionRepository.defaultAreaSize()
        val verticalGap = if (dimensionRepository.navigationBarHeight() > 0)
            VERTICAL_STANDARD_GAP else VERTICAL_STANDARD_GAP_WITHOUT_BOTTOM

        val progressiveMines = preferencesRepository.getProgressiveValue()

        val display = dimensionRepository.displaySize()
        val calculatedWidth = ((display.width / fieldSize).toInt() - HORIZONTAL_STANDARD_GAP)
        val calculatedHeight = ((display.height / fieldSize).toInt() - verticalGap)
        val finalWidth = calculatedWidth.coerceAtLeast(MIN_STANDARD_WIDTH)
        val finalHeight = calculatedHeight.coerceAtLeast(MIN_STANDARD_HEIGHT)
        val fieldArea = finalWidth * finalHeight
        val finalMines =
            ((fieldArea * CUSTOM_LEVEL_MINE_RATIO).toInt() + progressiveMines)
                .coerceAtMost((fieldArea * MAX_LEVEL_MINE_RATIO).toInt())

        return Minefield(finalWidth, finalHeight, finalMines)
    }

    override fun randomSeed(): Long = Random.nextLong()

    companion object {
        private val beginnerMinefield = Minefield(9, 9, 10)
        private val intermediateMinefield = Minefield(16, 16, 40)
        private val expertMinefield = Minefield(24, 24, 99)

        private const val CUSTOM_LEVEL_MINE_RATIO = 0.2
        private const val MAX_LEVEL_MINE_RATIO = 0.45
        private const val HORIZONTAL_STANDARD_GAP = 1
        private const val VERTICAL_STANDARD_GAP_WITHOUT_BOTTOM = 4
        private const val VERTICAL_STANDARD_GAP = 3
        private const val MIN_STANDARD_WIDTH = 6
        private const val MIN_STANDARD_HEIGHT = 9
    }
}
