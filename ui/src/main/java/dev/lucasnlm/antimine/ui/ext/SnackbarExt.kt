package dev.lucasnlm.antimine.ui.ext

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.utils.ContextExt.dpToPx
import dev.lucasnlm.antimine.utils.ContextExt.isPortrait

object SnackbarExt {
    /**
     * Shows a snackbar with a message.
     * Different of the default snackbar,
     * this one is shown above the bottom navigation bar.
     *
     * @param container The parent view to attach the snackbar to.
     * @param resId The message to show.
     * @param preferencesRepository The preferences repository to check for customizations.
     * @param duration The duration of the snackbar.
     */
    fun showWarning(
        container: View,
        @StringRes resId: Int,
        preferencesRepository: PreferencesRepository? = null,
        duration: Int = Snackbar.LENGTH_SHORT,
    ): Snackbar {
        val context = container.context
        return Snackbar.make(container, resId, duration).apply {
            if (context.isPortrait()) {
                if (preferencesRepository?.controlStyle() == ControlStyle.SwitchMarkOpen) {
                    view.translationY = -context.dpToPx(SNACK_BAR_VERTICAL_OFFSET_DP).toFloat()
                }
            }
            show()
        }
    }

    private const val SNACK_BAR_VERTICAL_OFFSET_DP = 128
}
