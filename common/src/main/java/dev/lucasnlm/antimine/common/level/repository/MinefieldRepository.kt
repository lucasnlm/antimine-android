package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield

/**
 * Get [Minefield]s based on the [Difficulty] and size.
 */
interface MinefieldRepository {
    /**
     * Get a [Minefield] based on the [Difficulty].
     */
    fun baseStandardSize(
        dimensionRepository: DimensionRepository,
        progressiveMines: Int,
        limitToMax: Boolean,
    ): Minefield

    /**
     * Get a [Minefield] based on the [Difficulty].
     */
    fun fromDifficulty(
        difficulty: Difficulty,
        dimensionRepository: DimensionRepository,
        preferencesRepository: PreferencesRepository,
    ): Minefield

    /**
     * @return A random seed for the minefield.
     */
    fun randomSeed(): Long
}
