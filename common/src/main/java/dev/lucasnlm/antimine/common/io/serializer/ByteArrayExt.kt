package dev.lucasnlm.antimine.common.io.serializer

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Mark
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
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
        val bytes = ByteArray(Long.SIZE_BYTES).apply {
            read(this)
        }
        return ByteBuffer.wrap(bytes).long
    }

    /**
     * Writes an [Area] to a [ByteArrayOutputStream].
     */
    fun ByteArrayOutputStream.writeArea(area: Area) {
        write(area.id)
        write(area.posX)
        write(area.posY)
        write(area.minesAround)
        write(area.hasMine.toInt())
        write(area.mistake.toInt())
        write(area.isCovered.toInt())
        write(area.mark.ordinal)
        write(area.revealed.toInt())
        write(area.neighborsIds.size)
        area.neighborsIds.forEach { neighborId ->
            write(neighborId)
        }
        write(area.dimNumber.toInt())
    }

    /**
     * Reads an [Area] from a [ByteArrayInputStream].
     * @return The read [Area].
     */
    fun ByteArrayInputStream.readArea(): Area {
        return Area(
            id = read(),
            posX = read(),
            posY = read(),
            minesAround = read(),
            hasMine = read() != 0,
            mistake = read() != 0,
            isCovered = read() != 0,
            mark = Mark.values()[read()],
            revealed = read() != 0,
            neighborsIds = List(read()) { read() },
            dimNumber = read() != 0,
        )
    }

    private fun Boolean.toInt() = if (this) 1 else 0
}
