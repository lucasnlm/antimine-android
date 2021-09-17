package dev.lucasnlm.antimine.ui

import android.app.Activity
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

fun Activity.showWarning(@StringRes text: Int): Snackbar {
    return Snackbar.make(
        findViewById(android.R.id.content),
        getString(text),
        Snackbar.LENGTH_SHORT
    ).apply {
        show()
    }
}
