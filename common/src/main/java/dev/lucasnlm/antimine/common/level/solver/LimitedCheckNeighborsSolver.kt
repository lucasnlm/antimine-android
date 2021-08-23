package dev.lucasnlm.antimine.common.level.solver

/**
 * Brute force solver that try solve a minefield checking
 * all neighbors with time limit.
 *
 * Bad point:
 *  - Solves only easy minefields.
 */
class LimitedCheckNeighborsSolver(
    private val maxAttemptTime: Long = DEFAULT_BRUTE_FORCE_TIMEOUT,
) : CheckNeighborsSolver() {

    private val initialTime = System.currentTimeMillis()

    override fun keepTrying(): Boolean {
        return (System.currentTimeMillis() - initialTime) <= maxAttemptTime
    }

    companion object {
        const val DEFAULT_BRUTE_FORCE_TIMEOUT = 1000L
    }
}
