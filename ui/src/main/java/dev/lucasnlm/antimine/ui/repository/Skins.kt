package dev.lucasnlm.antimine.ui.repository

import dev.lucasnlm.antimine.ui.model.AppSkin

object Skins {
    private fun default() = AppSkin(
        id = 0,
        file = "standard.png",
        canTint = true,
        isPaid = false,
    )

    private fun square() = AppSkin(
        id = 1,
        file = "square.png",
        canTint = true,
        isPaid = true,
    )

    private fun square2() = AppSkin(
        id = 2,
        file = "square-2.png",
        canTint = true,
        isPaid = true,
    )

    private fun square3() = AppSkin(
        id = 3,
        file = "square-3.png",
        canTint = true,
        isPaid = true,
    )

    private fun classic() = AppSkin(
        id = 4,
        file = "classic.png",
        canTint = true,
        isPaid = true,
    )

    private fun glass() = AppSkin(
        id = 5,
        file = "glass.png",
        canTint = true,
        isPaid = true,
    )

    private fun stone() = AppSkin(
        id = 6,
        file = "stone.png",
        canTint = true,
        isPaid = true,
    )

    fun getAllSkins() = listOf(
        default(),
        classic(),
        square(),
        square2(),
        square3(),
        glass(),
        stone(),
    )
}
