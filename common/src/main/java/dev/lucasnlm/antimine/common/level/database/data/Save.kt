package dev.lucasnlm.antimine.common.level.database.data

import androidx.room.*
import dev.lucasnlm.antimine.common.level.data.Area
import dev.lucasnlm.antimine.common.level.data.LevelSetup
import dev.lucasnlm.antimine.common.level.database.converters.FieldConverter
import dev.lucasnlm.antimine.common.level.database.converters.SaveStatusConverter

@Entity
data class Save(
    @PrimaryKey(autoGenerate = true)
    val uid: Int,

    @ColumnInfo(name = "seed")
    val seed: Long,

    @ColumnInfo(name = "date")
    val startDate: Long,

    @ColumnInfo(name = "duration")
    val duration: Long,

    @ColumnInfo(name = "width")
    val levelSetup: LevelSetup,

    @TypeConverters(SaveStatusConverter::class)
    @ColumnInfo(name = "status")
    val status: SaveStatus,

    @TypeConverters(FieldConverter::class)
    @ColumnInfo(name = "field")
    val field: List<Area>
)



