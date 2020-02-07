package dev.lucasnlm.antimine.common.level.data

data class LevelSetup(
    val width: Int,
    val height: Int,
    val mines: Int,
    val preset: DifficultyPreset = DifficultyPreset.Custom
)
