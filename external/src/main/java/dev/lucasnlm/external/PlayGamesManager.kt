package dev.lucasnlm.external

import android.app.Activity
import android.content.Intent

interface IPlayGamesManager {
    fun hasGooglePlayGames(): Boolean
    fun silentLogin(activity: Activity)
    fun getLoginIntent(): Intent?
    fun handleLoginResult(data: Intent?)
    fun isLogged(): Boolean
    fun openAchievements(activity: Activity)
    fun openLeaderboards(activity: Activity)
}
