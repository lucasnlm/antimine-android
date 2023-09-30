package dev.lucasnlm.antimine.common.io

import dev.lucasnlm.antimine.common.io.models.SaveFile
import dev.lucasnlm.antimine.common.io.serializer.SaveFileSerializer
import dev.lucasnlm.antimine.common.level.database.models.FirstOpen
import dev.lucasnlm.antimine.common.level.database.models.SaveStatus
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.models.Mark
import dev.lucasnlm.antimine.preferences.models.Minefield
import org.junit.Test
import kotlin.test.assertEquals

class SaveFileSerializerTest {
    private val area = Area(
        id = 10,
        posX = 25,
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

    private val saveFile = SaveFile(
        id = "some-id",
        seed = 12345678L,
        startDate = 12345678L,
        duration = 5678L,
        minefield = Minefield(
            width = 10,
            height = 21,
            mines = 30,
            seed = null,
        ),
        difficulty = Difficulty.Custom,
        firstOpen = FirstOpen.Position(5),
        status = SaveStatus.DEFEAT,
        field = listOf(area, area, area, area),
        actions = 345434,
    )

    @Test
    fun `serialize should return the same value as deserialize`() {
        val serialized = SaveFileSerializer.serialize(saveFile)
        val deserialized = SaveFileSerializer.deserialize(saveFile.id!!, serialized)
        assertEquals(saveFile, deserialized)
    }
}
