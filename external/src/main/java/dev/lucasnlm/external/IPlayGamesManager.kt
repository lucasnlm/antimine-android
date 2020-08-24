package dev.lucasnlm.external

import android.app.Activity
import android.content.Intent

enum class Achievement(
    val value: String
) {
    NoLuck(""),
    Almost(""),
    Beginner(""),
    Intermediate(""),
    Expert(""),
    ThirtySeconds(""),
    Flags(""),
    Boom("")
}

enum class Leaderboard(
    val value: String
) {
    BestTimeBeginner(""),
    BestTimeIntermediate(""),
    BestTimeExpert("")
}

interface IPlayGamesManager {
    fun hasGooglePlayGames(): Boolean
    fun silentLogin(activity: Activity)
    fun getLoginIntent(): Intent?
    fun handleLoginResult(data: Intent?)
    fun isLogged(): Boolean
    fun openAchievements(activity: Activity)
    fun openLeaderboards(activity: Activity)
    fun unlockAchievement(achievement: Achievement)
    fun incrementAchievement(achievement: Achievement)
    fun submitLeaderboard(leaderboard: Leaderboard, value: Long)
}