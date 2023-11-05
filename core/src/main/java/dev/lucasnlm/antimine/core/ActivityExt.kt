package dev.lucasnlm.antimine.core

import android.app.Activity
import android.os.Build
import androidx.activity.ComponentActivity

object ActivityExt {
    @Suppress("DEPRECATION")
    fun Activity.compatOverridePendingTransition() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overridePendingTransition(0, 0)
        } else {
            overrideActivityTransition(ComponentActivity.OVERRIDE_TRANSITION_OPEN, 0, 0)
            overrideActivityTransition(ComponentActivity.OVERRIDE_TRANSITION_CLOSE, 0, 0)
        }
    }
}
