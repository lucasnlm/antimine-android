package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield

class FixedMinefieldRepository : IMinefieldRepository {
    override fun baseStandardSize(
        dimensionRepository: IDimensionRepository,
        progressiveMines: Int,
    ): Minefield =
        Minefield(9, 9, 9 + progressiveMines)

    override fun fromDifficulty(
        difficulty: Difficulty,
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository,
    ): Minefield =
        Minefield(9, 9, 9)

    override fun randomSeed(): Long = 0L
}
