package dev.lucasnlm.antimine.common.io

import dev.lucasnlm.antimine.common.io.models.Stats
import dev.lucasnlm.antimine.common.io.serializer.StatsSerializer
import dev.lucasnlm.antimine.common.io.serializer.StatsSerializer.readStatsFile
import org.junit.Test
import java.io.DataInputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class StatsSerializerTest {
    private val stats =
        Stats(
            duration = 12345678L,
            mines = 10,
            victory = 1,
            width = 14,
            height = 9,
            openArea = 1234,
        )

    @Test
    fun `serialize should return the same value as deserialize`() {
        val serialized = StatsSerializer.serialize(stats)
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
        assertEquals(stats.duration, deserialized.duration)
        assertEquals(stats.mines, deserialized.mines)
        assertEquals(stats.victory, deserialized.victory)
        assertEquals(stats.width, deserialized.width)
        assertEquals(stats.height, deserialized.height)
        assertEquals(stats.openArea, deserialized.openArea)
    }
}
