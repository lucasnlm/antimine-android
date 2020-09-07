package dev.lucasnlm.antimine.common.level.solver

class LimitedBruteForceSolver(
    private val maxAttemptTime: Long = DEFAULT_BRUTE_FORCE_TIMEOUT,
) : BruteForceSolver() {

    private val initialTime = System.currentTimeMillis()

    override fun keepTrying(): Boolean {
        return (System.currentTimeMillis() - initialTime) <= maxAttemptTime
    }

    companion object {
        const val DEFAULT_BRUTE_FORCE_TIMEOUT = 500L
    }
}
