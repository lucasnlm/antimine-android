package dev.lucasnlm.antimine.core.haptic

interface HapticFeedbackManager {
    fun longPressFeedback()

    fun explosionFeedback()

    fun tutorialErrorFeedback()
}
