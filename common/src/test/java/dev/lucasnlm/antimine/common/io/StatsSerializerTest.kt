package dev.lucasnlm.antimine.common.io

import dev.lucasnlm.antimine.common.io.models.StatsFile
import dev.lucasnlm.antimine.common.io.serializer.StatsSerializer
import dev.lucasnlm.antimine.common.io.serializer.StatsSerializer.readStatsFile
import org.junit.Test
import java.io.DataInputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class StatsSerializerTest {
    private val statsFile =
        StatsFile(
            duration = 12345678L,
            mines = 10,
            victory = 1,
            width = 14,
            height = 9,
            openArea = 1234,
        )

    @Test
    fun `serialize should return the same value as deserialize`() {
        val serialized = StatsSerializer.serialize(statsFile)
        val deserialized =
            serialized.inputStream().use {
                DataInputStream(it).use { stream ->
                    stream.readStatsFile()
                }
            }

        val serializedArray = serialized.map { it.toInt() }
        assertEquals(
            listOf(0, 0, 0, 0, 0, -68, 97, 78, 0, 0, 0, 10, 0, 0, 0, 1, 0, 0, 0, 14, 0, 0, 0, 9, 0, 0, 4, -46),
            serializedArray,
        )

        assertNotNull(deserialized)
        assertEquals(statsFile.duration, deserialized.duration)
        assertEquals(statsFile.mines, deserialized.mines)
        assertEquals(statsFile.victory, deserialized.victory)
        assertEquals(statsFile.width, deserialized.width)
        assertEquals(statsFile.height, deserialized.height)
        assertEquals(statsFile.openArea, deserialized.openArea)
    }
}
