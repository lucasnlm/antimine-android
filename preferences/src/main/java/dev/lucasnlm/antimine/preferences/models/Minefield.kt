package dev.lucasnlm.antimine.preferences.models

import androidx.annotation.Keep

@Keep
data class Minefield(
    val width: Int,
    val height: Int,
    val mines: Int,
    val seed: Long? = null,
) {
    fun ratio(): Double = mines.toDouble() / (width * height)
}
