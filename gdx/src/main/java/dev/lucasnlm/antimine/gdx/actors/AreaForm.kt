package dev.lucasnlm.antimine.gdx.actors

data class AreaForm(
    val top: Boolean,
    val bottom: Boolean,
    val left: Boolean,
    val right: Boolean,
    val topLeft: Boolean = false,
    val topRight: Boolean  = false,
    val bottomLeft: Boolean = false,
    val bottomRight: Boolean = false,
)

val areaFullForm = AreaForm(top = true, bottom = true, left = true, right = true)

val areaNoForm = AreaForm(top = false, bottom = false, left = false, right = false)

fun allAreaForms() = (0..15).map {
    AreaForm(
        top = it and 0b1000 != 0x00,
        bottom = it and 0b0100 != 0x00,
        left = it and 0b0010 != 0x00,
        right = it and 0b0001 != 0x00,
    )
}.toList()
