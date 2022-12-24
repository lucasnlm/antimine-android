package dev.lucasnlm.antimine.gdx

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.GridPoint2
import dev.lucasnlm.antimine.gdx.models.RenderQuality

object GameTextureAtlas {
    private const val textureSize = 2048 / 8

    private fun gp(x: Int, y: Int): GridPoint2 {
        return GridPoint2(x, y)
    }

    private fun gridOf(vararg pairs: Pair<String, GridPoint2>): Map<String, GridPoint2> {
        return pairs.associate { (key, value) ->
            key to value.set(value.x * textureSize, value.y * textureSize)
        }
    }

    fun loadTextureAtlas(
        skinFile: String,
        quality: RenderQuality,
    ): TextureAtlas {
        val pixMapSize = when (quality) {
            RenderQuality.Low -> 512
            RenderQuality.Mid -> 1024
            RenderQuality.High -> 2048
        }

        val textureRegions = gridOf(
            AtlasNames.core to gp(x = 0, y = 0),
            AtlasNames.bottom to gp(x = 1, y = 0),
            AtlasNames.top to gp(x = 2, y = 0),
            AtlasNames.right to gp(x = 3, y = 0),
            AtlasNames.left to gp(x = 4, y = 0),
            AtlasNames.cornerTopLeft to gp(x = 5, y = 0),
            AtlasNames.cornerTopRight to gp(x = 6, y = 0),
            AtlasNames.cornerBottomRight to gp(x = 7, y = 0),
            AtlasNames.cornerBottomLeft to gp(x = 0, y = 1),
            AtlasNames.borderCornerTopRight to gp(x = 1, y = 1),
            AtlasNames.borderCornerTopLeft to gp(x = 2, y = 1),
            AtlasNames.borderCornerBottomRight to gp(x = 3, y = 1),
            AtlasNames.borderCornerBottomLeft to gp(x = 4, y = 1),
            AtlasNames.fillTopLeft to gp(x = 7, y = 3),
            AtlasNames.fillTopRight to gp(x = 6, y = 3),
            AtlasNames.fillBottomRight to gp(x = 0, y = 4),
            AtlasNames.fillBottomLeft to gp(x = 1, y = 4),
            AtlasNames.full to gp(x = 7, y = 6),
            AtlasNames.single to gp(x = 0, y = 2),
            AtlasNames.singleBackground to gp(x = 3, y = 7),
            AtlasNames.number1 to gp(x = 2, y = 2),
            AtlasNames.number2 to gp(x = 3, y = 2),
            AtlasNames.number3 to gp(x = 4, y = 2),
            AtlasNames.number4 to gp(x = 5, y = 2),
            AtlasNames.number5 to gp(x = 6, y = 2),
            AtlasNames.number6 to gp(x = 7, y = 2),
            AtlasNames.number7 to gp(x = 0, y = 3),
            AtlasNames.number8 to gp(x = 1, y = 3),
            AtlasNames.flag to gp(x = 2, y = 3),
            AtlasNames.mine to gp(x = 3, y = 3),
            AtlasNames.question to gp(x = 4, y = 3),
        )

        val texture = Texture(skinFile).apply {
            setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        }

        return TextureAtlas().apply {
            textureRegions.forEach { (name, position) ->
                addRegion(name, TextureRegion(texture, position.x, position.y, textureSize, textureSize))
            }
        }
    }
}
