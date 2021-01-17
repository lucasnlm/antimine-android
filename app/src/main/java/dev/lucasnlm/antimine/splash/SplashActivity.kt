package dev.lucasnlm.antimine.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import dev.lucasnlm.antimine.GameActivity
import dev.lucasnlm.antimine.TvGameActivity
import dev.lucasnlm.antimine.core.isAndroidTv
import dev.lucasnlm.antimine.splash.viewmodel.SplashViewModel
import dev.lucasnlm.external.IFeatureFlagManager
import dev.lucasnlm.external.IPlayGamesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : AppCompatActivity() {
    private val playGamesManager: IPlayGamesManager by inject()
    private val featureFlagManager: IFeatureFlagManager by inject()

    private val splashViewMode: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        splashViewMode.startIap()

        lifecycleScope.launchWhenCreated {
            featureFlagManager.refresh()
        }

        lifecycleScope.launchWhenCreated {
            if (playGamesManager.hasGooglePlayGames()) {
                var logged: Boolean

                try {
                    withContext(Dispatchers.IO) {
                        logged = playGamesManager.silentLogin()
                    }
                } catch (e: Exception) {
                    logged = false
                    Log.e(TAG, "Failed silent login", e)
                }

                try {
                    if (logged) {
                        migrateDateAndGoToGameActivity()
                    } else {
                        playGamesManager.getLoginIntent()?.let {
                            ActivityCompat.startActivityForResult(
                                this@SplashActivity, it,
                                GOOGLE_PLAY_REQUEST_CODE, null
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "User not logged or doesn't have Play Games", e)
                }
            } else {
                goToGameActivity()
            }
        }
    }

    private fun migrateDateAndGoToGameActivity() {
        lifecycleScope.launchWhenCreated {
            if (!isFinishing) {
                withContext(Dispatchers.IO) {
                    playGamesManager.playerId()?.let {
                        splashViewMode.migrateCloudSave(it)
                    }
                }

                goToGameActivity()
            }
        }
    }

    private fun goToGameActivity() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        if (applicationContext.isAndroidTv()) {
            Intent(this, TvGameActivity::class.java).run { startActivity(this) }
        } else {
            Intent(this, GameActivity::class.java).run { startActivity(this) }
        }

        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GameActivity.GOOGLE_PLAY_REQUEST_CODE) {
            playGamesManager.handleLoginResult(data)

            try {
                migrateDateAndGoToGameActivity()
            } catch (e: Exception) {
                Log.e(TAG, "User not logged or doesn't have Play Games", e)
            }
        }
    }

    companion object {
        val TAG = SplashActivity::class.simpleName
        private const val GOOGLE_PLAY_REQUEST_CODE = 6
    }
}
