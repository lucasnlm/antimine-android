package dev.lucasnlm.antimine.common.level.database.converters

import dev.lucasnlm.antimine.core.models.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Test

class DifficultyConverterTest {
    @Test
    fun toDifficulty() {
        val converter = DifficultyConverter()
        assertEquals(converter.toDifficulty(0), Difficulty.Standard)
        assertEquals(converter.toDifficulty(1), Difficulty.Beginner)
        assertEquals(converter.toDifficulty(2), Difficulty.Intermediate)
        assertEquals(converter.toDifficulty(3), Difficulty.Expert)
        assertEquals(converter.toDifficulty(4), Difficulty.Custom)
    }

    @Test(expected = IllegalArgumentException::class)
    fun toDifficultyInvalid() {
        val converter = DifficultyConverter()
        converter.toDifficulty(100)
    }

    @Test
    fun toInteger() {
        val converter = DifficultyConverter()
        assertEquals(converter.toInteger(Difficulty.Standard), 0)
        assertEquals(converter.toInteger(Difficulty.Beginner), 1)
        assertEquals(converter.toInteger(Difficulty.Intermediate), 2)
        assertEquals(converter.toInteger(Difficulty.Expert), 3)
        assertEquals(converter.toInteger(Difficulty.Custom), 4)
    }
}
