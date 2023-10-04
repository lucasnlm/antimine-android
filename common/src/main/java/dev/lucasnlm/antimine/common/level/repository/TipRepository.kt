package dev.lucasnlm.antimine.common.level.repository

/**
 * Repository for tips.
 */
interface TipRepository {
    /**
     * Set the extra tips.
     * @param amount The amount of extra tips.
     */
    fun setExtraTips(amount: Int)

    /**
     * Remove one tip.
     * Return true if successful.
     */
    fun removeTip(): Boolean

    /**
     * Increase the amount of tips.
     */
    fun increaseTip(amount: Int)

    /**
     * @return The amount of tips.
     */
    fun getTotalTips(): Int
}
