package dev.lucasnlm.antimine.gdx

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.GridPoint2

object GameTextureAtlas {
    private const val TEXTURE_SIZE = 2048 / 8

    private fun gp(
        x: Int,
        y: Int,
    ): GridPoint2 {
        return GridPoint2(x, y)
    }

    private fun gridOf(vararg pairs: Pair<String, GridPoint2>): Map<String, GridPoint2> {
        return pairs.associate { (key, value) ->
            key to value.set(value.x * TEXTURE_SIZE, value.y * TEXTURE_SIZE)
        }
    }

    fun loadTextureAtlas(
        skinFile: String,
        defaultBackground: Int,
    ): TextureAtlas {
        val textureRegions =
            gridOf(
                AtlasNames.CORE to gp(x = 0, y = 0),
                AtlasNames.BOTTOM to gp(x = 1, y = 0),
                AtlasNames.TOP to gp(x = 2, y = 0),
                AtlasNames.RIGHT to gp(x = 3, y = 0),
                AtlasNames.LEFT to gp(x = 4, y = 0),
                AtlasNames.CORNER_TOP_LEFT to gp(x = 5, y = 0),
                AtlasNames.CORNER_TOP_RIGHT to gp(x = 6, y = 0),
                AtlasNames.CORNER_BOTTOM_RIGHT to gp(x = 7, y = 0),
                AtlasNames.CORNER_BOTTOM_LEFT to gp(x = 0, y = 1),
                AtlasNames.BORDER_CORNER_RIGHT to gp(x = 1, y = 1),
                AtlasNames.BORDER_CORNER_LEFT to gp(x = 2, y = 1),
                AtlasNames.BORDER_CORNER_BOTTOM_RIGHT to gp(x = 3, y = 1),
                AtlasNames.BORDER_CORNER_BOTTOM_LEFT to gp(x = 4, y = 1),
                AtlasNames.FILL_TOP_LEFT to gp(x = 7, y = 4),
                AtlasNames.FILL_TOP_RIGHT to gp(x = 6, y = 4),
                AtlasNames.FILL_BOTTOM_RIGHT to gp(x = 0, y = 4),
                AtlasNames.FILL_BOTTOM_LEFT to gp(x = 1, y = 4),
                AtlasNames.FULL to gp(x = 7, y = 6),
                AtlasNames.SINGLE to gp(x = 0, y = 2),
                AtlasNames.SINGLE_BACKGROUND to gp(x = defaultBackground.coerceIn(0, 4), y = 7),
                AtlasNames.NUMBER_1 to gp(x = 2, y = 2),
                AtlasNames.NUMBER_2 to gp(x = 3, y = 2),
                AtlasNames.NUMBER_3 to gp(x = 4, y = 2),
                AtlasNames.NUMBER_4 to gp(x = 5, y = 2),
                AtlasNames.NUMBER_5 to gp(x = 6, y = 2),
                AtlasNames.NUMBER_6 to gp(x = 7, y = 2),
                AtlasNames.NUMBER_7 to gp(x = 0, y = 3),
                AtlasNames.NUMBER_8 to gp(x = 1, y = 3),
                AtlasNames.FLAG to gp(x = 2, y = 3),
                AtlasNames.MINE to gp(x = 3, y = 3),
                AtlasNames.QUESTION to gp(x = 4, y = 3),
            )

        val texture =
            Texture(skinFile).apply {
                setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
            }

        return TextureAtlas().apply {
            textureRegions.forEach { (name, position) ->
                addRegion(name, TextureRegion(texture, position.x, position.y, TEXTURE_SIZE, TEXTURE_SIZE))
            }
        }
    }
}
