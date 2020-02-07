package dev.lucasnlm.antimine.splash

import android.app.UiModeManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.lucasnlm.antimine.GameActivity
import dev.lucasnlm.antimine.TvGameActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uiModeManager: UiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
        if (uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) {
            Intent(this, TvGameActivity::class.java).run { startActivity(this) }
        } else {
            Intent(this, GameActivity::class.java).run { startActivity(this) }
        }

        finish()
    }
}
