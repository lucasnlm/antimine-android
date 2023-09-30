package dev.lucasnlm.antimine.common.io.models

/**
 * Used to define the position of the first open square.
 */
sealed class FirstOpen {
    /**
     * Used before the first step or before this value be recorded.
     */
    object Unknown : FirstOpen()

    /**
     * Describes the [value] of the first step.
     */
    data class Position(
        val value: Int,
    ) : FirstOpen()

    override fun toString(): String =
        when (this) {
            is Position -> value.toString()
            else -> "Unknown"
        }

    fun toInt(): Int =
        when (this) {
            is Position -> value
            else -> -1
        }
}
