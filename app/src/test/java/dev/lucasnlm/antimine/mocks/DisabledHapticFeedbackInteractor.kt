package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackInteractor

class DisabledHapticFeedbackInteractor : IHapticFeedbackInteractor {
    override fun longPressFeedback() { }

    override fun explosionFeedback() { }
}
