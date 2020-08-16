package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.games.Games


class PlayGamesManager(
    private val context: Context
) : IPlayGamesManager {
    private var account: GoogleSignInAccount? = null

    private fun setupPopUp(activity: Activity, account: GoogleSignInAccount) {
        Games.getGamesClient(context, account).setViewForPopups(activity.findViewById(android.R.id.content))
    }

    override fun hasGooglePlayGames(): Boolean = true

    override fun silentLogin(activity: Activity) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build()
        val lastAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

        if (lastAccount != null) {
            account = lastAccount.also { setupPopUp(activity, it) }
        } else {
            GoogleSignIn
                .getClient(context, signInOptions)
                .silentSignIn()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        account = task.result?.also { setupPopUp(activity, it) }
                    }
                }
        }
    }

    override fun getLoginIntent(): Intent? {
        val signInClient = GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
        return signInClient.signInIntent
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
}
