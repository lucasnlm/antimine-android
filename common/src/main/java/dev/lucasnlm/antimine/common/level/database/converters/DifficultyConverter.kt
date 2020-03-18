package dev.lucasnlm.antimine.common.level.database.converters

import androidx.room.TypeConverter
import dev.lucasnlm.antimine.common.level.models.Difficulty

class DifficultyConverter {

    @TypeConverter
    fun toDifficulty(difficulty: Int): Difficulty =
        when (difficulty) {
            0 -> Difficulty.Standard
            1 -> Difficulty.Beginner
            2 -> Difficulty.Intermediate
            3 -> Difficulty.Expert
            else -> throw IllegalArgumentException("Could not recognize Difficulty")
        }

    @TypeConverter
    fun toInteger(difficulty: Difficulty): Int = difficulty.ordinal
}
