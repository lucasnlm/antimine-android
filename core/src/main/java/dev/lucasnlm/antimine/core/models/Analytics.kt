package dev.lucasnlm.antimine.core.models

import dev.lucasnlm.antimine.preferences.models.Minefield

sealed class Analytics(
    val name: String,
    val extra: Map<String, String> = mapOf(),
) {
    object Open : Analytics("Open game")

    class NewGame(
        minefield: Minefield,
        difficulty: Difficulty,
        seed: Long,
    ) : Analytics(
        "New Game",
        mapOf(
            "Seed" to seed.toString(),
            "Difficulty Preset" to difficulty.text,
            "Width" to minefield.width.toString(),
            "Height" to minefield.height.toString(),
            "Mines" to minefield.mines.toString(),
        )
    )

    class RetryGame(
        minefield: Minefield,
        difficulty: Difficulty,
        seed: Long,
        firstOpen: Int,
    ) : Analytics(
        "Retry Game",
        mapOf(
            "Seed" to seed.toString(),
            "Difficulty Preset" to difficulty.text,
            "Width" to minefield.width.toString(),
            "Height" to minefield.height.toString(),
            "Mines" to minefield.mines.toString(),
            "First Open" to firstOpen.toString()
        )
    )

    data class ContinueGameAfterGameOver(
        val error: Int,
    ) : Analytics("Continue after game over", mapOf("error" to error.toString()))

    object ResumePreviousGame : Analytics("Resume previous game")

    class OpenTile(index: Int) : Analytics("Open Tile", mapOf("Index" to index.toString()))

    class OpenOrFlagTile(index: Int) : Analytics("Open or Flag Tile", mapOf("Index" to index.toString()))

    class SwitchMark(index: Int) : Analytics("Switch Mark", mapOf("Index" to index.toString()))

    class HighlightNeighbors(index: Int) : Analytics("Highlight Neighbors", mapOf("Index" to index.toString()))

    class OpenNeighbors(index: Int) : Analytics("Open Neighbors", mapOf("Index" to index.toString()))

    class GameOver(time: Long, score: Score) : Analytics(
        "Game Over",
        mapOf(
            "Time" to time.toString(),
            "Right Mines" to score.rightMines.toString(),
            "Total Mines" to score.totalMines.toString(),
            "Total Area" to score.totalArea.toString()
        )
    )

    class Victory(time: Long, score: Score, difficulty: Difficulty) : Analytics(
        "Victory",
        mapOf(
            "Time" to time.toString(),
            "Difficulty" to difficulty.text,
            "Right Mines" to score.rightMines.toString(),
            "Total Mines" to score.totalMines.toString(),
            "Total Area" to score.totalArea.toString()
        )
    )

    object Resume : Analytics("Back to the game")

    object Quit : Analytics("Quit game")

    object CloseEndGameScreen : Analytics("Closed End Game Screen")

    object OpenAbout : Analytics("Open About")

    object OpenStats : Analytics("Open Stats")

    object OpenTranslations : Analytics("Open Translations")

    object OpenGooglePlayGames : Analytics("Open Google Play Games")

    object OpenControls : Analytics("Open Controls")

    object OpenThemes : Analytics("Open Themes")

    object ClickEmoji : Analytics("Click Emoji")

    object ContinueGame : Analytics("Continue Game")

    object OpenTutorial : Analytics("Open Tutorial")

    data class KnowHowToPlay(
        private val known: Boolean,
    ) : Analytics("Know How To Play", mapOf("Known" to known.toString()))

    object OpenCustom : Analytics("Open Custom")

    object OpenAchievements : Analytics("Open Achievements")

    object OpenLeaderboards : Analytics("Open Leaderboards")

    data class ClickTheme(
        private val themeId: Long,
    ) : Analytics("Click Theme", mapOf("id" to themeId.toString()))

    object OpenSettings : Analytics("Open Settings")

    object OpenSaveHistory : Analytics("Open Save History")

    object RemoveAds : Analytics("Remove Ads")

    object UseTip : Analytics("Use Tip")

    class TapGameReset(resign: Boolean) : Analytics("Game reset", mapOf("Resign" to resign.toString()))
}
