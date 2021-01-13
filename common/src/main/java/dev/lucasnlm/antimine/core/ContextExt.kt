package dev.lucasnlm.antimine.core

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity

fun Context.isAndroidTv(): Boolean {
    val uiModeManager = getSystemService(AppCompatActivity.UI_MODE_SERVICE) as UiModeManager
    return (uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION)
}
