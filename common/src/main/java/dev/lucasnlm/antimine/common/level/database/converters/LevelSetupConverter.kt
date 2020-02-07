package dev.lucasnlm.antimine.common.level.database.converters

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.lucasnlm.antimine.common.level.data.DifficultyPreset
import dev.lucasnlm.antimine.common.level.data.LevelSetup

class LevelSetupConverter {
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter: JsonAdapter<LevelSetup>

    init {
        this.jsonAdapter = moshi.adapter(LevelSetup::class.java)
    }

    @TypeConverter
    fun toLevelSetup(jsonInput: String): LevelSetup =
        jsonAdapter.fromJson(jsonInput) ?: LevelSetup(9, 9, 10, DifficultyPreset.Beginner)

    @TypeConverter
    fun toJsonString(field: LevelSetup): String = jsonAdapter.toJson(field)
}
