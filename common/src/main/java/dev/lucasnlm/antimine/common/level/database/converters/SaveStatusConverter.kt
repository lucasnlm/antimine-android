package dev.lucasnlm.antimine.common.level.database.converters

import androidx.room.TypeConverter
import dev.lucasnlm.antimine.common.level.database.models.SaveStatus

class SaveStatusConverter {
    @TypeConverter
    fun toSaveStatus(status: Int): SaveStatus =
        when (status) {
            0 -> SaveStatus.ON_GOING
            1 -> SaveStatus.VICTORY
            2 -> SaveStatus.DEFEAT
            else -> throw IllegalArgumentException("Could not recognize SaveStatus")
        }

    @TypeConverter
    fun toInteger(status: SaveStatus): Int = status.code
}
