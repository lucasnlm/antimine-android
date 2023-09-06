package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.external.FeatureFlagManager

class TipRepositoryImpl(
    private val preferencesRepository: PreferencesRepository,
    private val featureFlagManager: FeatureFlagManager,
) : TipRepository {
    override fun setExtraTips(amount: Int) {
        preferencesRepository.setExtraTips(amount)
    }

    override fun removeTip(): Boolean {
        val tips = preferencesRepository.getTips()
        val extra = preferencesRepository.getExtraTips()

        return when {
            tips >= 1 -> {
                preferencesRepository.refreshLastHelpUsed()
                preferencesRepository.setTips(tips - 1)
                true
            }
            preferencesRepository.getExtraTips() >= 1 -> {
                preferencesRepository.setExtraTips(extra - 1)
                true
            }
            else -> {
                false
            }
        }
    }

    override fun increaseTip(amount: Int) {
        val newValue =
            (preferencesRepository.getTips() + amount)
                .coerceAtMost(getMaxTips())
                .coerceAtLeast(0)
        preferencesRepository.setTips(newValue)
    }

    override fun getTotalTips(): Int {
        return preferencesRepository.getExtraTips() + preferencesRepository.getTips()
    }

    private fun getMaxTips(): Int {
        return if (featureFlagManager.isFoss) MAX_TIPS_FOSS else MAX_TIPS_CLOSED
    }

    companion object {
        private const val MAX_TIPS_FOSS = 100
        private const val MAX_TIPS_CLOSED = 25
    }
}
