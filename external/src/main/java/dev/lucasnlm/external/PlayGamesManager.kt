package dev.lucasnlm.external

import android.app.Activity
import android.content.Intent

enum class Achievement(
    val value: String,
) {
    NoLuck(""),
    Almost(""),
    Beginner(""),
    Intermediate(""),
    Expert(""),
    ThirtySeconds(""),
    Flags(""),
    Boom(""),
}

enum class Leaderboard(
    val value: String,
) {
    BeginnerBestTime(""),
    IntermediateBestTime(""),
    ExpertBestTime(""),
    MasterBestTime(""),
    LegendaryBestTime(""),
}

interface PlayGamesManager {
    suspend fun playerId(): String?

    fun hasGooglePlayGames(): Boolean

    suspend fun silentLogin(): Boolean

    fun showPlayPopUp(activity: Activity)

    fun getLoginIntent(): Intent?

    fun handleLoginResult(data: Intent?)

    fun isLogged(): Boolean

    fun openAchievements(activity: Activity)

    fun openLeaderboards(activity: Activity)

    suspend fun unlockAchievement(achievement: Achievement)

    suspend fun incrementAchievement(
        achievement: Achievement,
        value: Int,
    )

    suspend fun setAchievementSteps(
        achievement: Achievement,
        value: Int,
    )

    fun submitLeaderboard(
        leaderboard: Leaderboard,
        value: Long,
    )

    fun keepRequestingLogin(status: Boolean)

    fun shouldRequestLogin(): Boolean
}
