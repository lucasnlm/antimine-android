package dev.lucasnlm.antimine.common.level.database.models

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import dev.lucasnlm.antimine.common.level.database.converters.AreaConverter
import dev.lucasnlm.antimine.common.level.database.converters.FirstOpenConverter
import dev.lucasnlm.antimine.common.level.database.converters.SaveStatusConverter
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.preferences.models.Minefield

@Keep
@Entity(tableName = "new_save")
data class Save(
    @PrimaryKey(autoGenerate = true)
    val uid: Int,
    @ColumnInfo(name = "seed")
    val seed: Long,
    @ColumnInfo(name = "date")
    val startDate: Long,
    @ColumnInfo(name = "duration")
    val duration: Long,
    @ColumnInfo(name = "minefield")
    val minefield: Minefield,
    @ColumnInfo(name = "difficulty")
    val difficulty: Difficulty,
    @TypeConverters(FirstOpenConverter::class)
    @ColumnInfo(name = "firstOpen")
    val firstOpen: FirstOpen,
    @TypeConverters(SaveStatusConverter::class)
    @ColumnInfo(name = "status")
    val status: SaveStatus,
    @TypeConverters(AreaConverter::class)
    @ColumnInfo(name = "field")
    val field: List<Area>,
    @ColumnInfo(name = "actions")
    val actions: Int,
)
