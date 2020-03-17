package dev.lucasnlm.antimine.common.level.data

sealed class Status {
    object PreGame : Status()

    object Running : Status()

    class Over(
        val time: Long = 0L,
        val score: Score
    ) : Status()
}
