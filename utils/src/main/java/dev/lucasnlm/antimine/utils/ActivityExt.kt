package dev.lucasnlm.antimine.utils

import android.app.Activity
import androidx.activity.ComponentActivity
import dev.lucasnlm.antimine.utils.BuildExt.androidUpsideDownCake

object ActivityExt {
    @Suppress("DEPRECATION")
    fun Activity.compatOverridePendingTransition() {
        when {
            androidUpsideDownCake() -> {
                overrideActivityTransition(ComponentActivity.OVERRIDE_TRANSITION_OPEN, 0, 0)
                overrideActivityTransition(ComponentActivity.OVERRIDE_TRANSITION_CLOSE, 0, 0)
            }
            else -> {
                overridePendingTransition(0, 0)
            }
        }
    }
}
