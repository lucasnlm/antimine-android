package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield

class FixedMinefieldRepository : MinefieldRepository {
    override fun baseStandardSize(
        dimensionRepository: DimensionRepository,
        progressiveMines: Int,
        limitToMax: Boolean,
    ): Minefield = Minefield(9, 9, 9 + progressiveMines)

    override fun fromDifficulty(
        difficulty: Difficulty,
        dimensionRepository: DimensionRepository,
        preferencesRepository: PreferencesRepository,
    ): Minefield = Minefield(9, 9, 9)

    override fun randomSeed(): Long = 0L
}
