package dev.lucasnlm.antimine.sgtatham

class SgTathamMines {
    /**
     * Uses the same minefield generator of SgTatham mines game.
     *
     * @param seed The seed to be used to generate the minefield.
     * @param sliceWidth The width of the slice.
     * @param width The width of the minefield.
     * @param height The height of the minefield.
     * @param mines The number of mines in the minefield.
     * @param x The x coordinate of the first click.
     * @param y The y coordinate of the first click.
     */
    external fun createMinefield(
        seed: Long,
        sliceWidth: Int,
        width: Int,
        height: Int,
        mines: Int,
        x: Int,
        y: Int,
    ): String

    companion object {
        // Used to load the 'sgtatham' library on application startup.
        init {
            System.loadLibrary("sgtatham")
        }
    }
}
