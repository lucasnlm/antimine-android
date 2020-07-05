package dev.lucasnlm.antimine.common.level.di

import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackManager

class DisabledIHapticFeedbackManager : IHapticFeedbackManager {
    override fun longPressFeedback() {
        // Empty
    }

    override fun explosionFeedback() {
        // Empty
    }
}

