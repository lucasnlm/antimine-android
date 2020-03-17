package dev.lucasnlm.antimine.common.level.models

enum class DifficultyPreset(
    val text: String
) {
    Standard("STANDARD"),
    Beginner("BEGINNER"),
    Intermediate("INTERMEDIATE"),
    Expert("EXPERT"),
    Custom("CUSTOM")
}
