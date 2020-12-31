package dev.lucasnlm.antimine.common.level.database.converters

import androidx.room.TypeConverter
import dev.lucasnlm.antimine.core.models.Difficulty

class DifficultyConverter {
    @TypeConverter
    fun toDifficulty(difficulty: Int): Difficulty =
        when (difficulty) {
            INT_STANDARD -> Difficulty.Standard
            INT_BEGINNER -> Difficulty.Beginner
            INT_INTERMEDIATE -> Difficulty.Intermediate
            INT_EXPERT -> Difficulty.Expert
            INT_CUSTOM -> Difficulty.Custom
            else -> throw IllegalArgumentException("Could not recognize Difficulty")
        }

    @TypeConverter
    fun toInteger(difficulty: Difficulty): Int = difficulty.ordinal

    companion object {
        const val INT_STANDARD = 0
        const val INT_BEGINNER = 1
        const val INT_INTERMEDIATE = 2
        const val INT_EXPERT = 3
        const val INT_CUSTOM = 4
    }
}
