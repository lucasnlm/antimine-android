package dev.lucasnlm.antimine.mocks

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
