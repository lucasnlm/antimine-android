package dev.lucasnlm.antimine.ui.ext

import android.graphics.Color
import androidx.annotation.ColorInt

@ColorInt
fun Int.toAndroidColor(alpha: Int? = null): Int {
    return if (alpha == null) {
        Color.rgb(
            Color.red(this),
            Color.green(this),
            Color.blue(this)
        )
    } else {
        Color.argb(
            160,
            Color.red(this),
            Color.green(this),
            Color.blue(this)
        )
    }
}

@ColorInt
fun Int.toInvertedAndroidColor(alpha: Int? = null): Int {
    return if (alpha == null) {
        Color.rgb(
            255 - Color.red(this),
            255 - Color.green(this),
            255 - Color.blue(this)
        )
    } else {
        Color.argb(
            160,
            255 - Color.red(this),
            255 - Color.green(this),
            255 - Color.blue(this)
        )
    }
}
