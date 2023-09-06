package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield

interface MinefieldRepository {
    fun baseStandardSize(
        dimensionRepository: DimensionRepository,
        progressiveMines: Int,
    ): Minefield

    fun fromDifficulty(
        difficulty: Difficulty,
        dimensionRepository: DimensionRepository,
        preferencesRepository: PreferencesRepository,
    ): Minefield

    fun randomSeed(): Long
}
