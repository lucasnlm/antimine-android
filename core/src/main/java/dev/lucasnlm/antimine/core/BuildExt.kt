package dev.lucasnlm.antimine.core

import android.annotation.SuppressLint
import android.os.Build

@SuppressLint("AnnotateVersionCheck")
object BuildExt {
    /**
     * Executes the block if the current Android version
     * is at least [Build.VERSION_CODES.O].
     */
    fun withAndroidOreo(block: () -> Unit) {
        withAndroidVersion(Build.VERSION_CODES.O, block)
    }

    private fun withAndroidVersion(version: Int, block: () -> Unit) {
        if (Build.VERSION.SDK_INT >= version) {
            block()
        }
    }
}
