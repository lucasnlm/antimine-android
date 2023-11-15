package dev.lucasnlm.antimine.wear.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.lucasnlm.antimine.utils.ActivityExt.compatOverridePendingTransition

class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().run {
            setOnExitAnimationListener {
                startMainActivity()
            }
        }
        super.onCreate(savedInstanceState)
    }

    private fun startMainActivity() {
        val intent =
            Intent(applicationContext, MainActivity::class.java).apply {
                addFlags(
                    listOf(
                        Intent.FLAG_ACTIVITY_NEW_TASK,
                        Intent.FLAG_ACTIVITY_CLEAR_TASK,
                        Intent.FLAG_ACTIVITY_NO_ANIMATION,
                    ).fold(0, Int::or),
                )
            }
        startActivity(intent)
        compatOverridePendingTransition()
    }
}
