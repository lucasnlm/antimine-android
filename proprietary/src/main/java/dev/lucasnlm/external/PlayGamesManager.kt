package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.Games
import com.google.android.gms.tasks.Tasks

class PlayGamesManager(
    private val context: Context,
) : IPlayGamesManager {
    private var account: GoogleSignInAccount? = null

    private fun setupPopUp(activity: Activity, account: GoogleSignInAccount) {
        Games.getGamesClient(context, account).apply {
            setViewForPopups(activity.findViewById(android.R.id.content))
            setGravityForPopups(Gravity.TOP or Gravity.END)
        }
    }

    override fun playerId(): String? {
        return account?.let {
            try {
                Tasks.await(Games.getPlayersClient(context, it).currentPlayerId)
            } catch (e: ApiException) {
                Log.e(TAG, "Fail to request current player id")
                account = null
                null
            }
        }
    }

    override fun showPlayPopUp(activity: Activity) {
        if (!activity.isFinishing) {
            account?.let {
                setupPopUp(activity, it)
            }
        }
    }

    override fun hasGooglePlayGames(): Boolean = true

    override fun silentLogin(): Boolean {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build()
        val lastAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)
        return try {
            val client = GoogleSignIn.getClient(context, signInOptions)
            account = lastAccount ?: Tasks.await(client.silentSignIn())
            account != null
        } catch (e: ApiException) {
            account = null
            false
        }
    }

    override fun getLoginIntent(): Intent? {
        return GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).signInIntent
    }

    override fun handleLoginResult(data: Intent?) {
        if (data != null) {
            Auth.GoogleSignInApi.getSignInResultFromIntent(data)?.let { result ->
                if (result.isSuccess) {
                    account = result.signInAccount
                } else {
                    result.status.statusMessage?.let { message ->
                        if (message.isNotBlank()) {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun isLogged(): Boolean = account != null

    override fun openAchievements(activity: Activity) {
        account?.let {
            Games.getAchievementsClient(context, it)
                .achievementsIntent
                .addOnSuccessListener { intent ->
                    activity.startActivityForResult(intent, 0)
                }
        }
    }

    override fun openLeaderboards(activity: Activity) {
        account?.let {
            Games.getLeaderboardsClient(context, it)
                .allLeaderboardsIntent
                .addOnSuccessListener { intent ->
                    activity.startActivityForResult(intent, 0)
                }
        }
    }

    override fun unlockAchievement(achievement: Achievement) {
        account?.let {
            Games.getAchievementsClient(context, it).unlock(achievement.value)
        }
    }

    override fun incrementAchievement(achievement: Achievement) {
        account?.let {
            Games.getAchievementsClient(context, it).increment(achievement.value, 1)
        }
    }

    override fun submitLeaderboard(leaderboard: Leaderboard, value: Long) {
        account?.let {
            Games.getLeaderboardsClient(context, it).submitScore(leaderboard.value, value)
        }
    }

    companion object {
        val TAG = PlayGamesManager::class.simpleName
    }
}
