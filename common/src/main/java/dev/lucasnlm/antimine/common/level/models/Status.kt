package dev.lucasnlm.antimine.common.level.models

sealed class Status {
    object PreGame : Status()

    object Running : Status()

    class Over(
        val time: Long = 0L,
        val score: dev.lucasnlm.antimine.core.models.Score? = null,
    ) : Status()
}
