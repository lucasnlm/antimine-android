package dev.lucasnlm.antimine.common.io

import android.content.Context
import dev.lucasnlm.antimine.common.io.serializer.StatsSerializer
import dev.lucasnlm.antimine.common.level.database.models.Stats

class StatsFileManagerImpl(
    private val context: Context,
) : StatsFileManager {
    override fun insert(stats: Stats) {
        val statsBytes = StatsSerializer.serialize(stats)
        context.filesDir.resolve(filePath).appendBytes(statsBytes)
    }

    override fun readStats(): List<Stats> {
        return context.filesDir.resolve(filePath).readBytes().let { stream ->
            val result = mutableListOf<Stats>()
            do {
                val stats = StatsSerializer.deserialize(stream)?.let {
                    result.add(it)
                }
            } while (stats != null)
            result
        }
    }

    companion object {
        private const val filePath = "stats"
    }
}
