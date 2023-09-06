package dev.lucasnlm.antimine.common.level.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.lucasnlm.antimine.common.level.database.converters.AreaConverter
import dev.lucasnlm.antimine.common.level.database.converters.DifficultyConverter
import dev.lucasnlm.antimine.common.level.database.converters.FirstOpenConverter
import dev.lucasnlm.antimine.common.level.database.converters.MinefieldConverter
import dev.lucasnlm.antimine.common.level.database.converters.SaveStatusConverter
import dev.lucasnlm.antimine.common.level.database.dao.SaveDao
import dev.lucasnlm.antimine.common.level.database.dao.StatsDao
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.database.models.Stats

@Database(
    entities = [
        Save::class,
        Stats::class,
    ],
    version = 8,
    exportSchema = false,
)
@TypeConverters(
    AreaConverter::class,
    SaveStatusConverter::class,
    MinefieldConverter::class,
    DifficultyConverter::class,
    FirstOpenConverter::class,
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun saveDao(): SaveDao

    abstract fun statsDao(): StatsDao

    companion object {
        const val DATA_BASE_NAME = "saves-db"
    }
}
