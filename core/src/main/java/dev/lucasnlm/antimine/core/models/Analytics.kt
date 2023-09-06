package dev.lucasnlm.antimine.core.models

import dev.lucasnlm.antimine.preferences.models.Minefield

sealed class Analytics(
    val name: String,
    val extra: Map<String, String> = mapOf(),
) {
    data object Open : Analytics("Open game")

    class NewGame(
        minefield: Minefield,
        difficulty: Difficulty,
        seed: Long,
    ) : Analytics(
            name = "New Game",
            extra =
                mapOf(
                    "Seed" to seed.toString(),
                    "Difficulty Preset" to difficulty.id,
                    "Width" to minefield.width.toString(),
                    "Height" to minefield.height.toString(),
                    "Mines" to minefield.mines.toString(),
                ),
        )

    class RetryGame(
        minefield: Minefield,
        difficulty: Difficulty,
        seed: Long,
        firstOpen: Int,
    ) : Analytics(
            name = "Retry Game",
            extra =
                mapOf(
                    "Seed" to seed.toString(),
                    "Difficulty Preset" to difficulty.id,
                    "Width" to minefield.width.toString(),
                    "Height" to minefield.height.toString(),
                    "Mines" to minefield.mines.toString(),
                    "First Open" to firstOpen.toString(),
                ),
        )

    data class ContinueGameAfterGameOver(
        val error: Int,
    ) : Analytics("Continue after game over", mapOf("error" to error.toString()))

    data object ResumePreviousGame : Analytics("Resume previous game")

    class OpenTile(index: Int) : Analytics("Open Tile", mapOf("Index" to index.toString()))

    class OpenOrFlagTile(index: Int) : Analytics("Open or Flag Tile", mapOf("Index" to index.toString()))

    class QuestionMark(index: Int) : Analytics("Question Mark on Tile", mapOf("Index" to index.toString()))

    class SwitchMark(index: Int) : Analytics("Switch Mark", mapOf("Index" to index.toString()))

    class OpenNeighbors(index: Int) : Analytics("Open Neighbors", mapOf("Index" to index.toString()))

    class OpenMusicLink(from: String) : Analytics("Open Music Link", mapOf("From" to from))

    class GameOver(time: Long, score: Score) : Analytics(
        name = "Game Over",
        extra =
            mapOf(
                "Time" to time.toString(),
                "Right Mines" to score.rightMines.toString(),
                "Total Mines" to score.totalMines.toString(),
                "Total Area" to score.totalArea.toString(),
            ),
    )

    class Victory(time: Long, score: Score, difficulty: Difficulty) : Analytics(
        name = "Victory",
        extra =
            mapOf(
                "Time" to time.toString(),
                "Difficulty" to difficulty.id,
                "Right Mines" to score.rightMines.toString(),
                "Total Mines" to score.totalMines.toString(),
                "Total Area" to score.totalArea.toString(),
            ),
    )

    data object Resume : Analytics("Back to the game")

    data object Quit : Analytics("Quit game")

    data object CloseEndGameScreen : Analytics("Closed End Game Screen")

    data object OpenAbout : Analytics("Open About")

    data object OpenStats : Analytics("Open Stats")

    data object OpenGooglePlayGames : Analytics("Open Google Play Games")

    data object OpenControls : Analytics("Open Controls")

    data object OpenThemes : Analytics("Open Themes")

    data object ClickEmoji : Analytics("Click Emoji")

    data object ContinueGame : Analytics("Continue Game")

    data object OpenTutorial : Analytics("Open Tutorial")

    data object OpenLanguage : Analytics("Open Language")

    data class KnowHowToPlay(
        private val known: Boolean,
    ) : Analytics("Know How To Play", mapOf("Known" to known.toString()))

    data object OpenCustom : Analytics("Open Custom")

    data object OpenAchievements : Analytics("Open Achievements")

    data object OpenLeaderboards : Analytics("Open Leaderboards")

    data class ClickTheme(
        private val themeId: Long,
    ) : Analytics("Click Theme", mapOf("id" to themeId.toString()))

    data class ClickSkin(
        private val skinId: Long,
    ) : Analytics("Click Skin", mapOf("id" to skinId.toString()))

    data object OpenSettings : Analytics("Open Settings")

    data object OpenSaveHistory : Analytics("Open Save History")

    data object RemoveAds : Analytics("Remove Ads")

    data object UseHint : Analytics("Use Tip")

    data object RequestMoreHints : Analytics("Request More Tip")

    class TapGameReset(resign: Boolean) : Analytics("Game reset", mapOf("Resign" to resign.toString()))
}
