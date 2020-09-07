package dev.lucasnlm.antimine.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.lucasnlm.antimine.GameActivity
import dev.lucasnlm.antimine.support.IapHandler
import org.koin.android.ext.android.inject

class SplashActivity : AppCompatActivity() {
    private val iapHandler: IapHandler by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        iapHandler.start()

        Intent(this, GameActivity::class.java).run { startActivity(this) }
        finish()
    }
}
