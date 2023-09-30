package dev.lucasnlm.antimine.common.io.serializer

import dev.lucasnlm.antimine.common.io.models.FileSave
import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.core.models.Area
import java.io.ByteArrayOutputStream

object StatsSerializer {
    fun serialize(stats: Stats): ByteArray {
        ByteArrayOutputStream(
            FileSave.BYTE_SIZE + (save.field.size * Area.BYTE_SIZE),
        )
    }

    fun deserialize(bytes: ByteArray): Stats? {

    }
}
