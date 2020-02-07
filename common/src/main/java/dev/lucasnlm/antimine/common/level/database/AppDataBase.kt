package dev.lucasnlm.antimine.common.level.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.lucasnlm.antimine.common.level.database.converters.FieldConverter
import dev.lucasnlm.antimine.common.level.database.converters.LevelSetupConverter
import dev.lucasnlm.antimine.common.level.database.converters.SaveStatusConverter
import dev.lucasnlm.antimine.common.level.database.dao.SaveDao
import dev.lucasnlm.antimine.common.level.database.data.Save

@Database(
    entities = [
        Save::class
    ], version = 1, exportSchema = false
)
@TypeConverters(
    FieldConverter::class,
    SaveStatusConverter::class,
    LevelSetupConverter::class
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun userDao(): SaveDao
}
