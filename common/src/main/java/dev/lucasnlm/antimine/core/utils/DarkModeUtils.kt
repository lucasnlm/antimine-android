package dev.lucasnlm.antimine.core.utils

import android.content.Context
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_NIGHT_MASK

fun isDarkModeEnabled(context: Context): Boolean {
    return when (context.resources.configuration.uiMode and UI_MODE_NIGHT_MASK) {
        UI_MODE_NIGHT_YES -> true
        else -> false
    }
}
