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

    @ColumnInfo(name = "durationPerMine")
    val durationPerMine: Long,

    @ColumnInfo(name = "mines")
    val mines: Long,

    @ColumnInfo(name = "victory")
    val victory: Long,

    @ColumnInfo(name = "victoryPercentage")
    val victoryPercentage: Long
)
