package dev.lucasnlm.antimine.common.io.serializer

import dev.lucasnlm.antimine.common.io.models.Stats
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

/**
 * Stats serializer.
 */
object StatsSerializer {
    /**
     * Serializes a [Stats] object into a [ByteArray].
     * @param stats The stats to be serialized.
     * @return The serialized stats.
     */
    fun serialize(stats: Stats): ByteArray {
        return ByteArrayOutputStream(
            Stats.BYTE_SIZE,
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
     * Deserializes a [ByteArray] into a [Stats] object.
     * @return The deserialized stats.
     */
    fun DataInputStream.readStatsFile(): Stats? {
        return run {
            if (available() < Stats.BYTE_SIZE) {
                return null
            } else {
                runCatching {
                    Stats(
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
