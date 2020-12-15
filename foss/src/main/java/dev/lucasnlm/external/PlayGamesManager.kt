package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.content.Intent

class PlayGamesManager(
    context: Context,
) : IPlayGamesManager {
    override fun playerId(): String? = null

    override fun hasGooglePlayGames(): Boolean = false

    override fun silentLogin(): Boolean {
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

    override fun unlockAchievement(achievement: Achievement) {
        // F-droid build doesn't have Google Play Games
    }

    override fun incrementAchievement(achievement: Achievement) {
        // F-droid build doesn't have Google Play Games
    }

    override fun submitLeaderboard(leaderboard: Leaderboard, value: Long) {
        // F-droid build doesn't have Google Play Games
    }
}
