package dev.lucasnlm.antimine.common.level.database.converters

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.lucasnlm.antimine.preferences.models.Minefield

class MinefieldConverter {
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter: JsonAdapter<Minefield> = moshi.adapter(
        Minefield::class.java
    )

    @TypeConverter
    fun toMinefield(jsonInput: String): Minefield =
        jsonAdapter.fromJson(jsonInput) ?: Minefield(9, 9, 9)

    @TypeConverter
    fun toJsonString(field: Minefield): String = jsonAdapter.toJson(field)
}
