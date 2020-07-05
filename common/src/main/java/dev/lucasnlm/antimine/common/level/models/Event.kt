package dev.lucasnlm.antimine.common.level.models

enum class Event {
    StartNewGame,
    ResumeGame,
    @Deprecated( "Use GameOver")
    ResumeGameOver,
    @Deprecated( "Use Victory")
    ResumeVictory,
    Pause,
    Resume,
    Running,
    Victory,
    GameOver
}
