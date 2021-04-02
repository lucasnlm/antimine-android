package dev.lucasnlm.antimine.gdx.models

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion

data class GameTextures(
    val detailedArea: Texture,
    val areaHighlight: Texture,
    val areaCovered: Texture,
    val aroundMines: List<TextureRegion>,
    val mine: TextureRegion,
    val flag: TextureRegion,
    val question: TextureRegion,
)
