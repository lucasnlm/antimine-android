package dev.lucasnlm.antimine.gdx.models

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import dev.lucasnlm.antimine.gdx.actors.AreaForm

data class GameTextures(
    val detailedArea: Texture,
    val detailedAreaOdd: Texture,
    val areaHighlight: Texture,
    val areaCovered: Texture,
    val areaCoveredOdd: Texture,
    val areaUncovered: Texture,
    val areaUncoveredOdd: Texture,
    val aroundMines: List<TextureRegion>,
    val areaTextures: Map<AreaForm, Texture>,
    val mine: TextureRegion,
    val flag: TextureRegion,
    val question: TextureRegion,
)
