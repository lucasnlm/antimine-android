package dev.lucasnlm.antimine.core.models

import androidx.annotation.Keep

@Keep
enum class Difficulty(
    val id: String,
) {
    Standard("STANDARD"),
    Beginner("BEGINNER"),
    Intermediate("INTERMEDIATE"),
    Expert("EXPERT"),
    Custom("CUSTOM"),
    Master("MASTER"),
    Legend("LEGEND"),
    FixedSize("FIXED_SIZE"),
}
