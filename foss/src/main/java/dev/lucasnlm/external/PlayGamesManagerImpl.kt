package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.content.Intent

class PlayGamesManagerImpl(
    context: Context,
) : PlayGamesManager {
    override suspend fun playerId(): String? = null

    override fun hasGooglePlayGames(): Boolean = false

    override suspend fun silentLogin(): Boolean {
        // F-droid build doesn't have Google Play Games
        return false
    }

    override fun showPlayPopUp(activity: Activity) {
        // F-droid build doesn't have Google Play Games
    }

    override fun getLoginIntent(): Intent? = null

    override fun handleLoginResult(data: Intent?) {
        // F-droid build doesn't have Google Play Games
    }

    override fun isLogged(): Boolean = false

    override fun openAchievements(activity: Activity) {
        // F-droid build doesn't have Google Play Games
    }

    override fun openLeaderboards(activity: Activity) {
        // F-droid build doesn't have Google Play Games
    }

    override suspend fun unlockAchievement(achievement: Achievement) {
        // F-droid build doesn't have Google Play Games
    }

    override suspend fun incrementAchievement(
        achievement: Achievement,
        value: Int,
    ) {
        // F-droid build doesn't have Google Play Games
    }

    override suspend fun setAchievementSteps(
        achievement: Achievement,
        value: Int,
    ) {
        // F-droid build doesn't have Google Play Games
    }

    override fun submitLeaderboard(
        leaderboard: Leaderboard,
        value: Long,
    ) {
        // F-droid build doesn't have Google Play Games
    }

    override fun keepRequestingLogin(status: Boolean) {
        // F-droid build doesn't have Google Play Games
    }

    override fun shouldRequestLogin(): Boolean {
        return false
    }
}
