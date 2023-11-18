package dev.lucasnlm.antimine.core.models

import androidx.annotation.Keep

@Keep
data class Area(
    val id: Int,
    val posX: Int,
    val posY: Int,
    val minesAround: Int = 0,
    val hasMine: Boolean = false,
    val mistake: Boolean = false,
    val isCovered: Boolean = true,
    val mark: Mark = Mark.None,
    val revealed: Boolean = false,
    val neighborsIds: List<Int>,
    val dimNumber: Boolean = false,
) {
    companion object {
        const val BYTE_SIZE = 20 * Int.SIZE_BYTES
    }

    val isOdd: Boolean =
        if (posY % 2 == 0) {
            posX % 2 != 0
        } else {
            posX % 2 == 0
        }
}
