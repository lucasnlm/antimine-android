package dev.lucasnlm.antimine.common.level.di

import dev.lucasnlm.antimine.core.haptic.HapticFeedbackManager

class DisabledHapticFeedbackManager : HapticFeedbackManager {
    override fun longPressFeedback() {
        // Empty
    }

    override fun explosionFeedback() {
        // Empty
    }

    override fun tutorialErrorFeedback() {
        // Empty
    }
}
