package dev.lucasnlm.antimine.playgames.viewmodel

sealed class PlayGamesEvent {
    object OpenAchievements : PlayGamesEvent()
    object OpenLeaderboards : PlayGamesEvent()
}
