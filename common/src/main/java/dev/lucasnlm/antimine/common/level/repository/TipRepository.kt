package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.external.IFeatureFlagManager

interface ITipRepository {
    fun setExtraTips(amount: Int)
    fun removeTip(): Boolean
    fun increaseTip(amount: Int)
    fun getTotalTips(): Int
}

class TipRepository(
    private val preferencesRepository: IPreferencesRepository,
    private val featureFlagManager: IFeatureFlagManager,
) : ITipRepository {
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
        return if (featureFlagManager.isFoss) 100 else 25
    }
}
