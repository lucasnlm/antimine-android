package dev.lucasnlm.antimine.common.io

import dev.lucasnlm.antimine.common.io.serializer.ByteArrayExt.readArea
import dev.lucasnlm.antimine.common.io.serializer.ByteArrayExt.writeArea
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Mark
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.test.assertEquals

class ByteArrayExtTest {
    private val area =
        Area(
            id = 10,
            posX = 20,
            posY = 30,
            minesAround = 6,
            hasMine = false,
            mistake = true,
            isCovered = false,
            mark = Mark.Flag,
            revealed = false,
            neighborsIds = listOf(11, 4, 22),
            dimNumber = false,
        )

    @Test
    fun `test Area serialization`() {
        ByteArrayOutputStream().use { stream ->
            DataOutputStream(stream).use { dataStream ->
                dataStream.writeArea(area)
            }

            val serializedArea = stream.toByteArray()
            assertEquals(56, serializedArea.size)

            ByteArrayInputStream(serializedArea).use { inputStream ->
                DataInputStream(inputStream).use { dataStream ->
                    val deserializedArea = dataStream.readArea()
                    assertEquals(area, deserializedArea)
                }
            }
        }
    }
}
