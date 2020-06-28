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

        val finalWidth = ((display.width / fieldSize).toInt() - 1).coerceAtLeast(6)
        val finalHeight = ((display.height / fieldSize).toInt() - 3).coerceAtLeast(9)

        return Minefield(
            finalWidth,
            finalHeight,
            (finalWidth * finalHeight * customLevelRatio).toInt()
        )
    }

    override fun randomSeed(): Long = Random.nextLong()

    companion object {
        private val beginnerMinefield = Minefield(9, 9, 10)
        private val intermediateMinefield = Minefield(16, 16, 40)
        private val expertMinefield = Minefield(24, 24, 99)
        private const val customLevelRatio = 0.2
    }
}
