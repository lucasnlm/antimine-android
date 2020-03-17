package dev.lucasnlm.antimine.core.analytics

import dev.lucasnlm.antimine.common.level.data.DifficultyPreset
import dev.lucasnlm.antimine.common.level.data.Score
import dev.lucasnlm.antimine.common.level.data.LevelSetup

sealed class Event(
    val title: String,
    val extra: Map<String, String> = mapOf()
) {
    class Open : Event("Open game")

    class NewGame(levelSetup: LevelSetup, seed: Long, useAccessibilityMode: Boolean) :
        Event("New Game", mapOf(
            "Seed" to seed.toString(),
            "Difficulty Preset" to levelSetup.preset.text,
            "Width" to levelSetup.width.toString(),
            "Height" to levelSetup.height.toString(),
            "Mines" to levelSetup.mines.toString(),
            "Accessibility" to useAccessibilityMode.toString()
            )
    )

    class ResumePreviousGame : Event("Resume previous game")

    class LongPressArea(index: Int) : Event("Long press area",
        mapOf("Index" to index.toString())
    )

    class LongPressMultipleArea(index: Int) : Event("Long press to open multiple",
        mapOf("Index" to index.toString())
    )

    class PressArea(index: Int) : Event("Press area",
        mapOf("Index" to index.toString())
    )

    class GameOver(time: Long, score: Score) : Event("Game Over",
        mapOf(
            "Time" to time.toString(),
            "Right Mines" to score.rightMines.toString(),
            "Total Mines" to score.totalMines.toString(),
            "Total Area" to score.totalArea.toString()
            )
    )

    class Victory(time: Long, score: Score, difficultyPreset: DifficultyPreset) : Event(
        "Victory",
        mapOf(
            "Time" to time.toString(),
            "Difficulty" to difficultyPreset.text,
            "Right Mines" to score.rightMines.toString(),
            "Total Mines" to score.totalMines.toString(),
            "Total Area" to score.totalArea.toString()
        )
    )

    class Resume : Event("Back to the game")

    class Quit : Event("Quit game")

    class OpenDrawer : Event("Opened Drawer")

    class CloseDrawer : Event("Closed Drawer")

    class OpenAbout : Event("Open About")

    class OpenSettings : Event("Open Settings")

    class ShowRatingRequest(usages: Int) : Event("Shown Rating Request",
        mapOf(
            "Usages" to usages.toString()
        ))

    class TapRatingRequest(from: String) : Event("Rating Request",
        mapOf(
            "From" to from
        ))

    class TapGameReset(resign: Boolean) : Event("Game reset",
        mapOf("Resign" to resign.toString())
    )
}
