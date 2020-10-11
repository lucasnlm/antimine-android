package dev.lucasnlm.antimine.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import dev.lucasnlm.antimine.GameActivity
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.splash.viewmodel.SplashViewModel
import dev.lucasnlm.external.IPlayGamesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : AppCompatActivity() {
    private val playGamesManager: IPlayGamesManager by inject()

    private val splashViewMode: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            fitsSystemWindows = true
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            orientation = LinearLayout.VERTICAL

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 200) }

            val loadingMessage = TextView(context).apply {
                text = context.getString(R.string.loading)
                setTextColor(ContextCompat.getColor(context, R.color.splash_text_color))
            }

            addView(loadingMessage, params)
        }
        setContentView(layout)

        splashViewMode.startIap()

        lifecycleScope.launchWhenCreated {
            if (playGamesManager.hasGooglePlayGames()) {
                withContext(Dispatchers.IO) {
                    try {
                        playGamesManager.silentLogin()
                        splashViewMode.migrateCloudSave()
                    } catch (e: Exception) {
                        Log.e(TAG, "User not logged in Play Games")
                    }
                }
            }

            withContext(Dispatchers.Main) {
                if (!isFinishing) {
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    Intent(this@SplashActivity, GameActivity::class.java)
                        .run { startActivity(this) }
                    finish()
                }
            }
        }
    }

    companion object {
        val TAG = SplashActivity::class.simpleName
    }
}
