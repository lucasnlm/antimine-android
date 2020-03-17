package dev.lucasnlm.antimine.core.analytics.models

import dev.lucasnlm.antimine.common.level.data.DifficultyPreset
import dev.lucasnlm.antimine.common.level.data.Score
import dev.lucasnlm.antimine.common.level.data.LevelSetup

sealed class Analytics(
    val title: String,
    val extra: Map<String, String> = mapOf()
) {
    class Open : Analytics("Open game")

    class NewGame(levelSetup: LevelSetup, seed: Long, useAccessibilityMode: Boolean) :
        Analytics("New Game", mapOf(
            "Seed" to seed.toString(),
            "Difficulty Preset" to levelSetup.preset.text,
            "Width" to levelSetup.width.toString(),
            "Height" to levelSetup.height.toString(),
            "Mines" to levelSetup.mines.toString(),
            "Accessibility" to useAccessibilityMode.toString()
            )
    )

    class ResumePreviousGame : Analytics("Resume previous game")

    class LongPressArea(index: Int) : Analytics("Long press area",
        mapOf("Index" to index.toString())
    )

    class LongPressMultipleArea(index: Int) : Analytics("Long press to open multiple",
        mapOf("Index" to index.toString())
    )

    class PressArea(index: Int) : Analytics("Press area",
        mapOf("Index" to index.toString())
    )

    class GameOver(time: Long, score: Score) : Analytics("Game Over",
        mapOf(
            "Time" to time.toString(),
            "Right Mines" to score.rightMines.toString(),
            "Total Mines" to score.totalMines.toString(),
            "Total Area" to score.totalArea.toString()
            )
    )

    class Victory(time: Long, score: Score, difficultyPreset: DifficultyPreset) : Analytics(
        "Victory",
        mapOf(
            "Time" to time.toString(),
            "Difficulty" to difficultyPreset.text,
            "Right Mines" to score.rightMines.toString(),
            "Total Mines" to score.totalMines.toString(),
            "Total Area" to score.totalArea.toString()
        )
    )

    class Resume : Analytics("Back to the game")

    class Quit : Analytics("Quit game")

    class OpenDrawer : Analytics("Opened Drawer")

    class CloseDrawer : Analytics("Closed Drawer")

    class OpenAbout : Analytics("Open About")

    class OpenSettings : Analytics("Open Settings")

    class ShowRatingRequest(usages: Int) : Analytics("Shown Rating Request",
        mapOf(
            "Usages" to usages.toString()
        ))

    class TapRatingRequest(from: String) : Analytics("Rating Request",
        mapOf(
            "From" to from
        ))

    class TapGameReset(resign: Boolean) : Analytics("Game reset",
        mapOf("Resign" to resign.toString())
    )
}
