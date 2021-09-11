package dev.lucasnlm.antimine.sgtatham

class SgTathamMines {
    /**
     * Uses the same minefield generator of SgTatham mines game.
     */
    external fun createMinefield(seed: String, width: Int, height: Int, mines: Int, x: Int, y: Int): String

    companion object {
        // Used to load the 'sgtatham' library on application startup.
        init {
            System.loadLibrary("sgtatham")
        }
    }
}
