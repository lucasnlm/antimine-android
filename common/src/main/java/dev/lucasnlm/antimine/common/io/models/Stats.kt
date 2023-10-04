package dev.lucasnlm.antimine.common.io.models

/**
 * Represents a stats file
 * @property duration The duration of the game
 * @property mines The number of mines in the game
 * @property victory The victory status of the game
 * @property width The width of the game
 * @property height The height of the game
 * @property openArea The number of open areas in the game
 */
data class Stats(
    val duration: Long,
    val mines: Int,
    val victory: Int,
    val width: Int,
    val height: Int,
    val openArea: Int,
) {
    fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            "duration" to duration.toString(),
            "mines" to mines.toString(),
            "victory" to victory.toString(),
            "width" to width.toString(),
            "height" to height.toString(),
            "openArea" to openArea.toString(),
        )

    companion object {
        const val BYTE_SIZE = Long.SIZE_BYTES + Int.SIZE_BYTES * 5
    }
}
