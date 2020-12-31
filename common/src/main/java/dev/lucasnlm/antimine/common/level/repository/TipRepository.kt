package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.preferences.IPreferencesRepository

interface ITipRepository {
    fun setExtraTips(amount: Int)
    fun removeTip(): Boolean
    fun increaseTip()
    fun getTotalTips(): Int
}

class TipRepository(
    private val preferencesRepository: IPreferencesRepository
) : ITipRepository {
    override fun setExtraTips(amount: Int) {
        preferencesRepository.setExtraTips(amount)
    }

    override fun removeTip(): Boolean {
        val tips = preferencesRepository.getTips()
        val extra = preferencesRepository.getExtraTips()

        return when {
            tips >= 1 -> {
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

    override fun increaseTip() {
        val newValue = (preferencesRepository.getTips() + 1).coerceAtMost(MAX_TIPS).coerceAtLeast(0)
        preferencesRepository.setTips(newValue)
    }

    override fun getTotalTips(): Int {
        return preferencesRepository.getExtraTips() + preferencesRepository.getTips()
    }

    companion object {
        const val MAX_TIPS = 15
    }
}
