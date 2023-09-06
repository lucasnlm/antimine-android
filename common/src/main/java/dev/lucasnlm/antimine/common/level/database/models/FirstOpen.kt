package dev.lucasnlm.antimine.common.level.database.models

import androidx.annotation.Keep

/**
 * Used to define the position of the first open square.
 */
@Keep
sealed class FirstOpen {
    /**
     * Used before the first step or before this value be recorded.
     */
    @Keep
    object Unknown : FirstOpen()

    /**
     * Describes the [value] of the first step.
     */
    @Keep
    class Position(
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
