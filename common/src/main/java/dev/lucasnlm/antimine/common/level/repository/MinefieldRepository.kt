package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository

interface IMinefieldRepository {
    fun fromDifficulty(
        difficulty: Difficulty,
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository
    ): Minefield
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
            Difficulty.Beginner -> Minefield(
                9,
                9,
                10
            )
            Difficulty.Intermediate -> Minefield(
                16,
                16,
                40
            )
            Difficulty.Expert -> Minefield(
                24,
                24,
                99
            )
            Difficulty.Custom -> preferencesRepository.customGameMode()
        }

    private fun calculateStandardMode(
        dimensionRepository: IDimensionRepository
    ): Minefield {
        val fieldSize = dimensionRepository.areaSize()

        val display = dimensionRepository.displaySize()
        val width = display.widthPixels
        val height = display.heightPixels

        val finalWidth = ((width / fieldSize).toInt() - 1).coerceAtLeast(6)
        val finalHeight = ((height / fieldSize).toInt() - 3).coerceAtLeast(9)

        return Minefield(
            finalWidth,
            finalHeight,
            (finalWidth * finalHeight * 0.2).toInt()
        )
    }
}
