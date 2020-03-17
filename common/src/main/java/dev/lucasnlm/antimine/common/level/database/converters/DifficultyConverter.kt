package dev.lucasnlm.antimine.common.level.database.converters

import androidx.room.TypeConverter
import dev.lucasnlm.antimine.common.level.data.DifficultyPreset

class DifficultyConverter {

    @TypeConverter
    fun toDifficulty(difficulty: Int): DifficultyPreset =
        when (difficulty) {
            0 -> DifficultyPreset.Standard
            1 -> DifficultyPreset.Beginner
            2 -> DifficultyPreset.Intermediate
            3 -> DifficultyPreset.Expert
            else -> throw IllegalArgumentException("Could not recognize Difficulty")
        }

    @TypeConverter
    fun toInteger(difficulty: DifficultyPreset): Int = difficulty.ordinal
}
