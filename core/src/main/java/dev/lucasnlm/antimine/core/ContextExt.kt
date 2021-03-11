package dev.lucasnlm.antimine.core

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

fun Context.isAndroidTv(): Boolean {
    val uiModeManager = getSystemService(AppCompatActivity.UI_MODE_SERVICE) as UiModeManager
    return (uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION)
}

fun Context.updateLanguage(localeName: String) {
    val localeStrList = localeName.split("-")
    val locale = Locale(localeStrList.first(), localeStrList.last())
    Locale.setDefault(locale)
    resources.apply {
        val config = resources.configuration.apply {
            setLocale(locale)
        }

        updateConfiguration(config, displayMetrics)
    }
}

fun Context.isPortrait(): Boolean {
    return resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}
