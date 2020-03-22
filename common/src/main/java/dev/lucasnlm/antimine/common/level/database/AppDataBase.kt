package dev.lucasnlm.antimine.common.level.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.lucasnlm.antimine.common.level.database.converters.DifficultyConverter
import dev.lucasnlm.antimine.common.level.database.converters.FieldConverter
import dev.lucasnlm.antimine.common.level.database.converters.MinefieldConverter
import dev.lucasnlm.antimine.common.level.database.converters.SaveStatusConverter
import dev.lucasnlm.antimine.common.level.database.dao.SaveDao
import dev.lucasnlm.antimine.common.level.database.models.Save

@Database(
    entities = [
        Save::class
    ], version = 2, exportSchema = false
)
@TypeConverters(
    FieldConverter::class,
    SaveStatusConverter::class,
    MinefieldConverter::class,
    DifficultyConverter::class
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun saveDao(): SaveDao
}
