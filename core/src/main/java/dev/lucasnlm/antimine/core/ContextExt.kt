package dev.lucasnlm.antimine.core

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

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

fun Context.dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}
