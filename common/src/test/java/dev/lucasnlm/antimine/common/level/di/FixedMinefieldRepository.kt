package dev.lucasnlm.antimine.common.level.di

import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield

class FixedMinefieldRepository : IMinefieldRepository {
    override fun baseStandardSize(dimensionRepository: IDimensionRepository, progressiveMines: Int): Minefield {
        return Minefield(9, 9, 9)
    }

    override fun fromDifficulty(
        difficulty: Difficulty,
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository,
    ) = Minefield(9, 9, 9)

    override fun randomSeed(): Long = 200
}
