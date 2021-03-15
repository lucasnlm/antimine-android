package dev.lucasnlm.antimine.gdx.models

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import dev.lucasnlm.antimine.gdx.actors.AreaForm

data class GameTextures(
    val detailedArea: Texture,
    val detailedAreaOdd: Texture,
    val areaCovered: List<Texture>,
    val areaCoveredOdd: List<Texture>,
    val areaUncovered: List<Texture>,
    val areaUncoveredOdd: List<Texture>,
    val aroundMines: List<TextureRegion>,
    val areaTextures: Map<AreaForm, Texture>,
    val mine: TextureRegion,
    val flag: TextureRegion,
    val question: TextureRegion,
)
