package dev.lucasnlm.antimine.utils

import android.content.Context
import android.content.res.Configuration

object ContextExt {
    /**
     * @return true if the device is in portrait mode, false otherwise.
     */
    fun Context.isPortrait(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    /**
     * @return true if the device is in landscape mode, false otherwise.
     */
    fun Context.dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
