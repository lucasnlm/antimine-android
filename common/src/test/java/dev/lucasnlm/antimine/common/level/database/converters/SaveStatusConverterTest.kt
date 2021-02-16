package dev.lucasnlm.antimine.common.level.database.converters

import dev.lucasnlm.antimine.common.level.database.models.SaveStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveStatusConverterTest {
    @Test
    fun toSaveStatus() {
        val converter = SaveStatusConverter()
        assertEquals(SaveStatus.ON_GOING, converter.toSaveStatus(0))
        assertEquals(SaveStatus.VICTORY, converter.toSaveStatus(1))
        assertEquals(SaveStatus.DEFEAT, converter.toSaveStatus(2))
    }

    @Test(expected = IllegalArgumentException::class)
    fun toSaveStatusInvalid() {
        val converter = SaveStatusConverter()
        converter.toSaveStatus(5)
    }

    @Test
    fun toInteger() {
        val converter = SaveStatusConverter()
        assertEquals(0, converter.toInteger(SaveStatus.ON_GOING))
        assertEquals(1, converter.toInteger(SaveStatus.VICTORY))
        assertEquals(2, converter.toInteger(SaveStatus.DEFEAT))
    }
}
