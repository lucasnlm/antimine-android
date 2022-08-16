package dev.lucasnlm.antimine.ui.ext

import android.graphics.Color
import androidx.annotation.ColorInt

fun Int.red(): Float = Color.red(this) / 255f
fun Int.green(): Float = Color.green(this) / 255f
fun Int.blue(): Float = Color.blue(this) / 255f
fun Int.alpha(): Float = Color.alpha(this) / 255f

@ColorInt
fun Int.toAndroidColor(alpha: Int? = null): Int {
    return if (alpha == null) {
        Color.rgb(
            Color.red(this),
            Color.green(this),
            Color.blue(this),
        )
    } else {
        Color.argb(
            alpha,
            Color.red(this),
            Color.green(this),
            Color.blue(this),
        )
    }
}

@ColorInt
fun Int.toInvertedAndroidColor(alpha: Int? = null): Int {
    return if (alpha == null) {
        Color.rgb(
            255 - Color.red(this),
            255 - Color.green(this),
            255 - Color.blue(this),
        )
    } else {
        Color.argb(
            alpha,
            255 - Color.red(this),
            255 - Color.green(this),
            255 - Color.blue(this),
        )
    }
}
