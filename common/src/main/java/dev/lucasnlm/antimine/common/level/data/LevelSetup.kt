package dev.lucasnlm.antimine.common.level.data

import androidx.annotation.Keep
import dev.lucasnlm.antimine.common.level.models.DifficultyPreset

@Keep
data class LevelSetup(
    val width: Int,
    val height: Int,
    val mines: Int,
    val preset: DifficultyPreset = DifficultyPreset.Custom
)
