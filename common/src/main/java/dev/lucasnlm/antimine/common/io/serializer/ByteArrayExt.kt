package dev.lucasnlm.antimine.common.io.serializer

import dev.lucasnlm.antimine.common.io.serializer.ByteArrayExt.toInt
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Mark
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer

object ByteArrayExt {
    /**
     * Writes a long value to a [ByteArrayOutputStream].
     * @param value The value to be written.
     */
    fun ByteArrayOutputStream.writeLong(value: Long) {
        val bytes = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(value).array()
        write(bytes)
    }

    /**
     * Reads a long value from a [ByteArrayInputStream].
     * @return The read value.
     */
    fun ByteArrayInputStream.readLong(): Long {
        val bytes =
            ByteArray(Long.SIZE_BYTES).apply {
                read(this)
            }
        return ByteBuffer.wrap(bytes).long
    }

    /**
     * Writes an [Area] to a [ByteArrayOutputStream].
     */
    fun DataOutputStream.writeArea(area: Area) {
        writeInt(area.id)
        writeInt(area.posX)
        writeInt(area.posY)
        writeInt(area.minesAround)
        writeInt(area.hasMine.toInt())
        writeInt(area.mistake.toInt())
        writeInt(area.isCovered.toInt())
        writeInt(area.mark.ordinal)
        writeInt(area.revealed.toInt())
        writeInt(area.neighborsIds.size)
        area.neighborsIds.forEach { neighborId ->
            writeInt(neighborId)
        }
        writeInt(area.dimNumber.toInt())
    }

    /**
     * Reads an [Area] from a [ByteArrayInputStream].
     * @return The read [Area].
     */
    fun DataInputStream.readArea(): Area {
        return Area(
            id = readInt(),
            posX = readInt(),
            posY = readInt(),
            minesAround = readInt(),
            hasMine = readInt() != 0,
            mistake = readInt() != 0,
            isCovered = readInt() != 0,
            mark = Mark.values()[readInt()],
            revealed = readInt() != 0,
            neighborsIds = List(readInt()) { readInt() },
            dimNumber = readInt() != 0,
        )
    }

    private fun Boolean.toInt() = if (this) 1 else 0
}
