package dev.lucasnlm.antimine.common.io.serializer

import dev.lucasnlm.antimine.common.io.models.StatsFile
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

/**
 * Stats serializer.
 */
object StatsSerializer {
    /**
     * Serializes a [StatsFile] object into a [ByteArray].
     * @param stats The stats to be serialized.
     * @return The serialized stats.
     */
    fun serialize(stats: StatsFile): ByteArray {
        return ByteArrayOutputStream(
            StatsFile.BYTE_SIZE,
        ).use {
            DataOutputStream(it).use { dataOutputStream ->
                dataOutputStream.writeLong(stats.duration)
                dataOutputStream.writeInt(stats.mines)
                dataOutputStream.writeInt(stats.victory)
                dataOutputStream.writeInt(stats.width)
                dataOutputStream.writeInt(stats.height)
                dataOutputStream.writeInt(stats.openArea)
            }
            it.toByteArray()
        }
    }

    /**
     * Deserializes a [ByteArray] into a [StatsFile] object.
     * @return The deserialized stats.
     */
    fun DataInputStream.readStatsFile(): StatsFile? {
        return run {
            if (available() < StatsFile.BYTE_SIZE) {
                return null
            } else {
                runCatching {
                    StatsFile(
                        duration = readLong(),
                        mines = readInt(),
                        victory = readInt(),
                        width = readInt(),
                        height = readInt(),
                        openArea = readInt(),
                    )
                }.getOrNull()
            }
        }
    }
}
