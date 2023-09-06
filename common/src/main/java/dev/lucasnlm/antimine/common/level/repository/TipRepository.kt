package dev.lucasnlm.antimine.common.level.repository

interface TipRepository {
    fun setExtraTips(amount: Int)

    fun removeTip(): Boolean

    fun increaseTip(amount: Int)

    fun getTotalTips(): Int
}
