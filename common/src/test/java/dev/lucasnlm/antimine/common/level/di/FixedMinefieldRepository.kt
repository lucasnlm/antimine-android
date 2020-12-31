package dev.lucasnlm.antimine.common.level.di

import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.preferences.IPreferencesRepository

class FixedMinefieldRepository : IMinefieldRepository {
    override fun fromDifficulty(
        difficulty: Difficulty,
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository
    ) = Minefield(9, 9, 9)

    override fun randomSeed(): Long = 200
}
