package dev.lucasnlm.antimine.common.level.di

import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield

class FixedMinefieldRepository : MinefieldRepository {
    override fun baseStandardSize(
        dimensionRepository: DimensionRepository,
        progressiveMines: Int,
    ): Minefield {
        return Minefield(9, 9, 9)
    }

    override fun fromDifficulty(
        difficulty: Difficulty,
        dimensionRepository: DimensionRepository,
        preferencesRepository: PreferencesRepository,
    ) = Minefield(9, 9, 9)

    override fun randomSeed(): Long = 200
}
