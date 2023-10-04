package dev.lucasnlm.antimine.common.io.models

enum class SaveStatus(
    val code: Int,
) {
    // Not finished game.
    ON_GOING(0),

    // Finished game with victory.
    VICTORY(1),

    // Finished game with game over.
    DEFEAT(2),
}
