package dev.lucasnlm.antimine.common.io.serializer

import dev.lucasnlm.antimine.common.io.models.FileSave
import dev.lucasnlm.antimine.common.io.serializer.ByteArrayExt.readArea
import dev.lucasnlm.antimine.common.io.serializer.ByteArrayExt.readLong
import dev.lucasnlm.antimine.common.io.serializer.ByteArrayExt.writeArea
import dev.lucasnlm.antimine.common.io.serializer.ByteArrayExt.writeLong
import dev.lucasnlm.antimine.common.level.database.models.FirstOpen
import dev.lucasnlm.antimine.common.level.database.models.SaveStatus
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.preferences.models.Minefield
import java.io.ByteArrayInputStream
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

    fun deserialize(saveId: String, content: ByteArray): FileSave {
        return ByteArrayInputStream(content).use { stream ->
            stream.run {
                val seed = readLong()
                val startDate = readLong()
                val duration = readLong()
                val difficulty = Difficulty.values()[read()]
                val firstOpen = read().let {
                    if (it < 0) {
                        FirstOpen.Unknown
                    } else {
                        FirstOpen.Position(it)
                    }
                }
                val status = SaveStatus.values()[read()]
                val actions = read()
                val minefield = Minefield(
                    width = read(),
                    height = read(),
                    mines = read(),
                    seed = readLong().let {
                        if (it == 0L) {
                            null
                        } else {
                            it
                        }
                    },
                )
                val fieldSize = read()
                val field = mutableListOf<Area>()
                for (it in 0 until fieldSize) {
                    field.add(readArea())
                }

                FileSave(
                    id = saveId,
                    seed = seed,
                    startDate = startDate,
                    duration = duration,
                    minefield = minefield,
                    difficulty = difficulty,
                    firstOpen = firstOpen,
                    status = status,
                    actions = actions,
                    field = field,
                )
            }
        }
    }
}
