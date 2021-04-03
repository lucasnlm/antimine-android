package dev.lucasnlm.antimine.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.utils.GdxNativesLoader
import dev.lucasnlm.antimine.GameActivity
import dev.lucasnlm.antimine.TvGameActivity
import dev.lucasnlm.antimine.core.isAndroidTv
import dev.lucasnlm.antimine.main.MainActivity
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.splash.viewmodel.SplashViewModel
import dev.lucasnlm.external.IFeatureFlagManager
import dev.lucasnlm.external.IPlayGamesManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : AppCompatActivity() {
    private val splashViewModel: SplashViewModel by viewModel()
    private val featureFlagManager: IFeatureFlagManager by inject()
    private val preferencesRepository: IPreferencesRepository by inject()
    private val playGamesManager: IPlayGamesManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        GdxNativesLoader.load()

        super.onCreate(savedInstanceState)

        splashViewModel.startIap()

        lifecycleScope.launchWhenCreated {
            featureFlagManager.refresh()
        }

        goToMainActivity()
    }

    private fun goToMainActivity() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        if (applicationContext.isAndroidTv()) {
            Intent(this, TvGameActivity::class.java).run { startActivity(this) }
        } else {
            val playGames = playGamesManager.hasGooglePlayGames()
            if ((playGames && preferencesRepository.userId() != null || !playGames) &&
                preferencesRepository.openGameDirectly()
            ) {
                Intent(this, GameActivity::class.java).run { startActivity(this) }
            } else {
                Intent(this, MainActivity::class.java).run { startActivity(this) }
            }
        }

        finish()
    }

    companion object {
        val TAG = SplashActivity::class.simpleName
    }
}
