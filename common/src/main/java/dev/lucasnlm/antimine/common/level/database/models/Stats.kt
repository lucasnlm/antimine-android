package dev.lucasnlm.antimine.common.level.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Stats(
    @PrimaryKey(autoGenerate = true)
    val uid: Int,

    @ColumnInfo(name = "duration")
    val duration: Long,

    @ColumnInfo(name = "mines")
    val mines: Int,

    @ColumnInfo(name = "victory")
    val victory: Int,

    @ColumnInfo(name = "width")
    val width: Int,

    @ColumnInfo(name = "height")
    val height: Int,

    @ColumnInfo(name = "openArea")
    val openArea: Int
)
