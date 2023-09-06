package dev.lucasnlm.antimine.ui.ext

import android.app.Activity
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import dev.lucasnlm.antimine.core.dpToPx

fun Activity.showWarning(
    @StringRes text: Int,
    isSwitchMarkOpen: Boolean = false,
): Snackbar {
    return Snackbar.make(
        findViewById(android.R.id.content),
        getString(text),
        Snackbar.LENGTH_SHORT,
    ).apply {
        if (isSwitchMarkOpen) {
            view.translationY = -dpToPx(SNACK_BAR_VERTICAL_OFFSET_DP).toFloat()
        }

        show()
    }
}

private const val SNACK_BAR_VERTICAL_OFFSET_DP = 128
