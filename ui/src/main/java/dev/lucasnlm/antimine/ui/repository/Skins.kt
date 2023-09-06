package dev.lucasnlm.antimine.ui.repository

import dev.lucasnlm.antimine.ui.R
import dev.lucasnlm.antimine.ui.model.AppSkin

object Skins {
    private fun default() =
        AppSkin(
            id = 0,
            file = "standard.png",
            canTint = true,
            isPremium = false,
            hasPadding = true,
            thumbnailImageRes = R.drawable.skin_standard,
        )

    private fun square() =
        AppSkin(
            id = 1,
            file = "square.png",
            canTint = true,
            isPremium = true,
            hasPadding = true,
            thumbnailImageRes = R.drawable.skin_square,
        )

    private fun square2() =
        AppSkin(
            id = 2,
            file = "square-2.png",
            canTint = true,
            isPremium = true,
            hasPadding = true,
            thumbnailImageRes = R.drawable.skin_square_2,
        )

    private fun square3() =
        AppSkin(
            id = 3,
            file = "square-3.png",
            canTint = true,
            isPremium = true,
            hasPadding = false,
            thumbnailImageRes = R.drawable.skin_square_3,
            showPadding = false,
        )

    private fun classic() =
        AppSkin(
            id = 4,
            file = "classic.png",
            canTint = true,
            isPremium = true,
            hasPadding = true,
            thumbnailImageRes = R.drawable.skin_classic,
            background = 4,
        )

    private fun classic2() =
        AppSkin(
            id = 5,
            file = "classic.png",
            canTint = false,
            isPremium = true,
            hasPadding = true,
            thumbnailImageRes = R.drawable.skin_classic,
            background = 4,
        )

    private fun glass() =
        AppSkin(
            id = 6,
            file = "glass.png",
            canTint = true,
            isPremium = true,
            hasPadding = true,
            thumbnailImageRes = R.drawable.skin_glass_2,
        )

    private fun stone() =
        AppSkin(
            id = 7,
            file = "stone.png",
            canTint = false,
            isPremium = true,
            hasPadding = true,
            thumbnailImageRes = R.drawable.skin_stone,
            background = 4,
        )

    private fun stone2() =
        AppSkin(
            id = 8,
            file = "stone-2.png",
            canTint = true,
            isPremium = true,
            hasPadding = true,
            thumbnailImageRes = R.drawable.skin_stone_2,
            background = 4,
        )

    private fun defaultNoJoin() =
        AppSkin(
            id = 9,
            file = "standard.png",
            canTint = true,
            isPremium = true,
            hasPadding = false,
            thumbnailImageRes = R.drawable.skin_standard_no_connection,
            showPadding = false,
        )

    fun getAllSkins() =
        listOf(
            default(),
            classic(),
            classic2(),
            stone(),
            glass(),
            square(),
            square2(),
            square3(),
            stone2(),
            defaultNoJoin(),
        )
}
