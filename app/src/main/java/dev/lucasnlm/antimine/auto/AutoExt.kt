package dev.lucasnlm.antimine.auto

import android.content.Context
import dev.lucasnlm.antimine.R

object AutoExt {
    fun Context.isAndroidAuto(): Boolean {
        return getString(R.string.androidAuto) == "1"
    }
}
