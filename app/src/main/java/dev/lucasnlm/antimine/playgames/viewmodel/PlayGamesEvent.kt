package dev.lucasnlm.antimine.playgames.viewmodel

sealed class PlayGamesEvent {
    data object OpenAchievements : PlayGamesEvent()

    data object OpenLeaderboards : PlayGamesEvent()
}
