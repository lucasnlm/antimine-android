package dev.lucasnlm.antimine.common.level

import dev.lucasnlm.antimine.common.level.data.DifficultyPreset
import dev.lucasnlm.antimine.common.level.data.LevelSetup
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository

object GameModeFactory {
    fun fromDifficultyPreset(
        difficulty: DifficultyPreset,
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository
    ): LevelSetup =
        when (difficulty) {
            DifficultyPreset.Standard -> calculateStandardMode(dimensionRepository)
            DifficultyPreset.Beginner -> LevelSetup(9, 9, 10, difficulty)
            DifficultyPreset.Intermediate -> LevelSetup(16, 16, 40, difficulty)
            DifficultyPreset.Expert -> LevelSetup(24, 24, 99, difficulty)
            DifficultyPreset.Custom -> preferencesRepository.customGameMode()
        }

    private fun calculateStandardMode(
        dimensionRepository: IDimensionRepository
    ): LevelSetup {
        val fieldSize = dimensionRepository.areaSize()

        val display = dimensionRepository.displaySize()
        val width = display.widthPixels
        val height = display.heightPixels

        val finalWidth = ((width / fieldSize).toInt() - 1).coerceAtLeast(6)
        val finalHeight = ((height / fieldSize).toInt() - 3).coerceAtLeast(9)

        return LevelSetup(
            finalWidth,
            finalHeight,
            (finalWidth * finalHeight * 0.2).toInt(),
            DifficultyPreset.Standard
        )
    }
}
