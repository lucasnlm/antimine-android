package dev.lucasnlm.antimine.common.level.database.converters

import androidx.room.TypeConverter
import dev.lucasnlm.antimine.common.level.database.models.FirstOpen

class FirstOpenConverter {
    @TypeConverter
    fun toFirstOpen(value: Int): FirstOpen =
        when {
            (value < 0) -> FirstOpen.Unknown
            else -> FirstOpen.Position(value)
        }

    @TypeConverter
    fun toInteger(firstOpen: FirstOpen): Int = firstOpen.toInt()
}
