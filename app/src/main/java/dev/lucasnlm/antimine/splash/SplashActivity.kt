package dev.lucasnlm.antimine.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.lucasnlm.antimine.GameActivity
import dev.lucasnlm.antimine.support.IapHandler
import dev.lucasnlm.external.IBillingManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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
