package dev.lucasnlm.antimine.ui.model

data class AppSkin(
    val id: Long,
    val file: String,
    val canTint: Boolean,
    val isPaid: Boolean = true,
)
