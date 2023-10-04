package dev.lucasnlm.antimine.common.io.serializer

import dev.lucasnlm.antimine.common.io.models.FirstOpen
import dev.lucasnlm.antimine.common.io.models.Save
import dev.lucasnlm.antimine.common.io.models.SaveStatus
import dev.lucasnlm.antimine.common.io.serializer.ByteArrayExt.readArea
import dev.lucasnlm.antimine.common.io.serializer.ByteArrayExt.writeArea
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.preferences.models.Minefield
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

/**
 * Save file serializer.
 */
object SaveFileSerializer {
    /**
     * Serializes a [Save] object into a [ByteArray].
     * @param save The save to be serialized.
     * @return The serialized save.
     */
    fun serialize(save: Save): ByteArray {
        return ByteArrayOutputStream(
            Save.BYTE_SIZE + (save.field.size * Area.BYTE_SIZE),
        ).use {
            DataOutputStream(it).use { stream ->
                stream.run {
                    writeLong(save.seed)
                    writeLong(save.startDate)
                    writeLong(save.duration)
                    writeInt(save.difficulty.ordinal)
                    writeInt(save.firstOpen.toInt())
                    writeInt(save.status.ordinal)
                    writeInt(save.actions)
                    writeInt(save.minefield.width)
                    writeInt(save.minefield.height)
                    writeInt(save.minefield.mines)
                    writeLong(save.minefield.seed ?: 0L)
                    writeInt(save.field.size)

                    save.field.forEach { area ->
                        writeArea(area)
                    }
                }
            }

            it.toByteArray()
        }
    }

    /**
     * Deserializes a [ByteArray] into a [Save] object.
     * @param saveId The save id.
     * @param content The content to be deserialized.
     * @return The deserialized save.
     */
    fun deserialize(
        saveId: String,
        content: ByteArray,
    ): Save {
        return ByteArrayInputStream(content).use {
            DataInputStream(it).use { stream ->
                stream.run {
                    val seed = readLong()
                    val startDate = readLong()
                    val duration = readLong()
                    val difficulty = Difficulty.values()[readInt()]
                    val firstOpen =
                        readInt().let { readValue ->
                            if (readValue < 0) {
                                FirstOpen.Unknown
                            } else {
                                FirstOpen.Position(readValue)
                            }
                        }
                    val status = SaveStatus.values()[readInt()]
                    val actions = readInt()
                    val minefield =
                        Minefield(
                            width = readInt(),
                            height = readInt(),
                            mines = readInt(),
                            seed =
                                readLong().let { readValue ->
                                    if (readValue == 0L) {
                                        null
                                    } else {
                                        readValue
                                    }
                                },
                        )

                    val field =
                        List(readInt()) {
                            readArea()
                        }

                    Save(
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
}
