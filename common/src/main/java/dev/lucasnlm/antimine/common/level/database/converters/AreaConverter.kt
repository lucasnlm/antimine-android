package dev.lucasnlm.antimine.common.level.database.converters

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.lucasnlm.antimine.core.models.Area
import java.lang.reflect.Type

class AreaConverter {
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter: JsonAdapter<List<Area>>

    init {
        val type: Type = Types.newParameterizedType(List::class.java, Area::class.java)
        this.jsonAdapter = moshi.adapter(type)
    }

    @TypeConverter
    fun toAreaList(jsonInput: String): List<Area> = jsonAdapter.fromJson(jsonInput) ?: listOf()

    @TypeConverter
    fun toJsonString(field: List<Area>): String = jsonAdapter.toJson(field)
}
