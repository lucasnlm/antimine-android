package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.preferences.IPreferencesRepository

class FixedMinefieldRepository : IMinefieldRepository {
    override fun fromDifficulty(
        difficulty: Difficulty,
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository
    ): Minefield =
        Minefield(9, 9, 9)

    override fun randomSeed(): Long = 0L
}
