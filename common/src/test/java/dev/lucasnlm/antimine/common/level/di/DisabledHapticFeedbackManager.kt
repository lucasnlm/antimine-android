package dev.lucasnlm.antimine.common.level.di

import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackManager

class DisabledHapticFeedbackManager : IHapticFeedbackManager {
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
