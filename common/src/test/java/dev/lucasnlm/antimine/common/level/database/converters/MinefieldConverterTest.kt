package dev.lucasnlm.antimine.common.level.database.converters

import dev.lucasnlm.antimine.preferences.models.Minefield
import org.junit.Assert.assertEquals
import org.junit.Test

class MinefieldConverterTest {
    private val expectedJson = "{\"width\":3,\"height\":3,\"mines\":4}"
    private val expectedMinefield = Minefield(3, 3, 4)

    @Test
    fun toMinefield() {
        val converter = MinefieldConverter()
        val minefield = converter.toMinefield(expectedJson)
        assertEquals(expectedMinefield, minefield)
    }

    @Test
    fun toJsonString() {
        val converter = MinefieldConverter()
        val json = converter.toJsonString(expectedMinefield)
        assertEquals(expectedJson, json)
    }
}
