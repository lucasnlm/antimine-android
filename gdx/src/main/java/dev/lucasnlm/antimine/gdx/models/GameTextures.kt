package dev.lucasnlm.antimine.gdx.models

import com.badlogic.gdx.graphics.g2d.TextureRegion

data class GameTextures(
    val detailedArea: TextureRegion,
    val areaBackground: TextureRegion,
    val aroundMines: List<TextureRegion>,
    val mine: TextureRegion,
    val flag: TextureRegion,
    val question: TextureRegion,
    val pieces: Map<String, TextureRegion>,
)
