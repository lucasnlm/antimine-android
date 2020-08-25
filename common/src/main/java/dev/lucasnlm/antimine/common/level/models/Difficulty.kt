package dev.lucasnlm.antimine.common.level.models

enum class Difficulty(
    val text: String,
) {
    Standard("STANDARD"),
    Beginner("BEGINNER"),
    Intermediate("INTERMEDIATE"),
    Expert("EXPERT"),
    Custom("CUSTOM"),
}
