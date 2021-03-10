package dev.lucasnlm.antimine.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.backends.android.surfaceview.GdxEglConfigChooser
import com.badlogic.gdx.utils.GdxNativesLoader
import dev.lucasnlm.antimine.GameActivity
import dev.lucasnlm.antimine.TvGameActivity
import dev.lucasnlm.antimine.isAndroidTv
import dev.lucasnlm.antimine.main.MainActivity
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.splash.viewmodel.SplashViewModel
import dev.lucasnlm.external.IFeatureFlagManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : AppCompatActivity() {
    private val featureFlagManager: IFeatureFlagManager by inject()
    private val preferencesRepository: IPreferencesRepository by inject()
    private val splashViewModel: SplashViewModel by viewModel()

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
            if (preferencesRepository.userId() != null && preferencesRepository.openGameDirectly()) {
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
