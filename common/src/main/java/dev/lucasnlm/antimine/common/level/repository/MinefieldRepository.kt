package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import kotlin.random.Random

interface IMinefieldRepository {
    fun fromDifficulty(
        difficulty: Difficulty,
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository
    ): Minefield

    fun randomSeed(): Long
}

class MinefieldRepository : IMinefieldRepository {
    override fun fromDifficulty(
        difficulty: Difficulty,
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository
    ): Minefield =
        when (difficulty) {
            Difficulty.Standard -> calculateStandardMode(
                dimensionRepository
            )
            Difficulty.Beginner -> beginnerMinefield
            Difficulty.Intermediate -> intermediateMinefield
            Difficulty.Expert -> expertMinefield
            Difficulty.Custom -> preferencesRepository.customGameMode()
        }

    private fun calculateStandardMode(
        dimensionRepository: IDimensionRepository
    ): Minefield {
        val fieldSize = dimensionRepository.areaSize()

        val display = dimensionRepository.displaySize()
        val calculatedWidth = ((display.width / fieldSize).toInt() - HORIZONTAL_STANDARD_GAP)
        val calculatedHeight = ((display.height / fieldSize).toInt() - VERTICAL_STANDARD_GAP)
        val finalWidth = calculatedWidth.coerceAtLeast(MIN_STANDARD_WIDTH)
        val finalHeight = calculatedHeight.coerceAtLeast(MIN_STANDARD_HEIGHT)
        val finalMines = (finalWidth * finalHeight * CUSTOM_LEVEL_RATIO).toInt()

        return Minefield(finalWidth, finalHeight, finalMines)
    }

    override fun randomSeed(): Long = Random.nextLong()

    companion object {
        private val beginnerMinefield = Minefield(9, 9, 10)
        private val intermediateMinefield = Minefield(16, 16, 40)
        private val expertMinefield = Minefield(24, 24, 99)

        private const val CUSTOM_LEVEL_RATIO = 0.2
        private const val HORIZONTAL_STANDARD_GAP = 1
        private const val VERTICAL_STANDARD_GAP = 3
        private const val MIN_STANDARD_WIDTH = 6
        private const val MIN_STANDARD_HEIGHT = 9
    }
}
