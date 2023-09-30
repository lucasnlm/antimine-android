package dev.lucasnlm.antimine.common.io.models

data class StatsFile(
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
