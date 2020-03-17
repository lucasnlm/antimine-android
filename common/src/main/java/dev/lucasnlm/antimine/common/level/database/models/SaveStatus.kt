package dev.lucasnlm.antimine.common.level.database.models

import androidx.room.TypeConverters
import dev.lucasnlm.antimine.common.level.database.converters.SaveStatusConverter

@TypeConverters(SaveStatusConverter::class)
enum class SaveStatus constructor(val code: Int) {
    ON_GOING(0),
    VICTORY(1),
    DEFEAT(2)
}
