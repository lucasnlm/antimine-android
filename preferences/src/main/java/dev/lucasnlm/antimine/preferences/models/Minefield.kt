package dev.lucasnlm.antimine.preferences.models

import androidx.annotation.Keep

@Keep
data class Minefield(
    val width: Int,
    val height: Int,
    val mines: Int,
    val seed: Long? = null,
) {
    private fun ratio(): Double = mines.toDouble() / (width * height)

    fun ratioPercent(): Int = (ratio() * 100.0).toInt()

    fun serialize(): String {
        return listOf<Any>(
            width,
            height,
            mines,
            seed ?: "null",
        ).joinToString(SEPARATOR) {
            it.toString()
        }
    }

    companion object {
        private const val SEPARATOR = ","

        /**
         * Parses a minefield from a string.
         * @param content The string to parse.
         * @return The parsed minefield or null if it's invalid.
         */
        fun fromString(content: String): Minefield? {
            return runCatching {
                val (width, height, mines, seed) = content.split(",")
                Minefield(
                    width = width.toIntOrNull() ?: throw IllegalArgumentException("Invalid width"),
                    height = height.toIntOrNull() ?: throw IllegalArgumentException("Invalid height"),
                    mines = mines.toIntOrNull() ?: throw IllegalArgumentException("Invalid mines"),
                    seed = seed.toLongOrNull(),
                )
            }.getOrNull()
        }
    }
}
