package dev.lucasnlm.antimine.common.io.serializer

import dev.lucasnlm.antimine.core.models.Area
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

object ByteArrayExt {
    fun ByteArrayOutputStream.writeLong(value: Long) {
        val bytes = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(value).array()
        write(bytes)
    }

    fun ByteArrayOutputStream.writeArea(area: Area) {

    }
}
