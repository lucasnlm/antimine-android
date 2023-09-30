package dev.lucasnlm.antimine.common.io.serializer

import dev.lucasnlm.antimine.common.io.models.FileSave
import dev.lucasnlm.antimine.common.io.serializer.ByteArrayExt.writeArea
import dev.lucasnlm.antimine.common.io.serializer.ByteArrayExt.writeLong
import dev.lucasnlm.antimine.core.models.Area
import java.io.ByteArrayOutputStream

object FileSaveSerializer {
    fun serialize(save: FileSave): ByteArray {
        return ByteArrayOutputStream(
            FileSave.BYTE_SIZE + (save.field.size * Area.BYTE_SIZE),
        ).use { stream ->
            stream.run {
                writeLong(save.seed)
                writeLong(save.startDate)
                writeLong(save.duration)
                write(save.difficulty.ordinal)
                write(save.firstOpen.toInt())
                write(save.status.ordinal)
                write(save.actions)
                write(save.minefield.width)
                write(save.minefield.height)
                write(save.minefield.mines)
                writeLong(save.minefield.seed ?: 0L)
                write(save.field.size)

                save.field.forEach { area ->
                    writeArea(area)
                }
            }
            stream.toByteArray()
        }
    }

    fun deserialize(content: String): FileSave? {
        return FileSave.fromString(content)
    }
}
