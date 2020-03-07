package dev.lucasnlm.antimine.common.level.data

sealed class GameStatus {
    object PreGame : GameStatus()

    object Running : GameStatus()

    class Over(val rightMines: Int = 0,
               val totalMines: Int = 0,
               val time: Long = 0
    ) : GameStatus()
}
