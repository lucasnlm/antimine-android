package dev.lucasnlm.antimine.ui.ext

import android.graphics.Color
import androidx.annotation.ColorInt

object ColorExt {
    private const val MAX_COLOR_VALUE_FLOAT = 255f
    private const val MAX_COLOR_VALUE_INT = 255

    /** Get the Red value of a color between 0 and 1 */
    fun Int.red(): Float = Color.red(this) / MAX_COLOR_VALUE_FLOAT

    /** Get the Green value of a color between 0 and 1 */
    fun Int.green(): Float = Color.green(this) / MAX_COLOR_VALUE_FLOAT

    /** Get the Blue value of a color between 0 and 1 */
    fun Int.blue(): Float = Color.blue(this) / MAX_COLOR_VALUE_FLOAT

    /** Get the Alpha value of a color between 0 and 1 */
    fun Int.alpha(): Float = Color.alpha(this) / MAX_COLOR_VALUE_FLOAT

    /**
     * Convert a Int to a Android Color.
     * @param alpha The alpha value of the color, between 0 and 255
     */
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

    /**
     * Convert a Int to a Android Color, inverting the color.
     * @param alpha The alpha value of the color, between 0 and 255
     */
    @ColorInt
    fun Int.toInvertedAndroidColor(alpha: Int? = null): Int {
        return if (alpha == null) {
            Color.rgb(
                MAX_COLOR_VALUE_INT - Color.red(this),
                MAX_COLOR_VALUE_INT - Color.green(this),
                MAX_COLOR_VALUE_INT - Color.blue(this),
            )
        } else {
            Color.argb(
                alpha,
                MAX_COLOR_VALUE_INT - Color.red(this),
                MAX_COLOR_VALUE_INT - Color.green(this),
                MAX_COLOR_VALUE_INT - Color.blue(this),
            )
        }
    }
}
