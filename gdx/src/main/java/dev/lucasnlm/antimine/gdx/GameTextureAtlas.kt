package dev.lucasnlm.antimine.gdx

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.GridPoint2
import dev.lucasnlm.antimine.gdx.models.RenderQuality

object GameTextureAtlas {
    private fun gridPoint(x: Int, y: Int): GridPoint2 {
        return GridPoint2(x, y)
    }

    fun loadTextureAtlas(
        radiusLevel: Int,
        squareDivider: Float,
        quality: RenderQuality,
    ): TextureAtlas {
        val pixMapSize = when (quality) {
            RenderQuality.Low -> 512
            RenderQuality.Mid -> 1024
            RenderQuality.High -> 2048
        }
        val textureSize = 2048 / 8

        val textureRegions = mapOf(
            AtlasNames.core to gridPoint(x = 0, y = 0),
            AtlasNames.bottom to gridPoint(x = textureSize, y = 0),
            AtlasNames.top to gridPoint(x = textureSize * 2, y = 0),
            AtlasNames.right to gridPoint(x = textureSize * 3, y = 0),
            AtlasNames.left to gridPoint(x = textureSize * 4, y = 0),
            AtlasNames.cornerTopLeft to gridPoint(x = textureSize * 5, y = 0),
            AtlasNames.cornerTopRight to gridPoint(x = textureSize * 6, y = 0),
            AtlasNames.cornerBottomRight to gridPoint(x = textureSize * 7, y = 0),
            AtlasNames.cornerBottomLeft to gridPoint(x = 0, y = textureSize),
            AtlasNames.borderCornerTopRight to gridPoint(x = textureSize, y = textureSize),
            AtlasNames.borderCornerTopLeft to gridPoint(x = textureSize * 2, y = textureSize),
            AtlasNames.borderCornerBottomRight to gridPoint(x = textureSize * 3, y = textureSize),
            AtlasNames.borderCornerBottomLeft to gridPoint(x = textureSize * 4, y = textureSize),
            AtlasNames.fillTopLeft to gridPoint(x = textureSize * 7, y = textureSize * 3),
            AtlasNames.fillTopRight to gridPoint(x = textureSize * 6, y = textureSize * 3),
            AtlasNames.fillBottomRight to gridPoint(x = 0, y = textureSize * 4),
            AtlasNames.fillBottomLeft to gridPoint(x = textureSize, y = textureSize * 4),
            AtlasNames.full to gridPoint(x = textureSize * 7, y = textureSize * 6),
            AtlasNames.single to gridPoint(x = 0, y = textureSize * 2),
            AtlasNames.singleBackground to gridPoint(x = textureSize, y = textureSize * 2),
            AtlasNames.number1 to gridPoint(x = textureSize * 2, y = textureSize * 2),
            AtlasNames.number2 to gridPoint(x = textureSize * 3, y = textureSize * 2),
            AtlasNames.number3 to gridPoint(x = textureSize * 4, y = textureSize * 2),
            AtlasNames.number4 to gridPoint(x = textureSize * 5, y = textureSize * 2),
            AtlasNames.number5 to gridPoint(x = textureSize * 6, y = textureSize * 2),
            AtlasNames.number6 to gridPoint(x = textureSize * 7, y = textureSize * 2),
            AtlasNames.number7 to gridPoint(x = 0, y = textureSize * 3),
            AtlasNames.number8 to gridPoint(x = textureSize, y = textureSize * 3),
            AtlasNames.flag to gridPoint(x = textureSize * 2, y = textureSize * 3),
            AtlasNames.mine to gridPoint(x = textureSize * 3, y = textureSize * 3),
            AtlasNames.question to gridPoint(x = textureSize * 4, y = textureSize * 3),
        )

        val texture = Texture(TextureTheme.square2).apply {
            setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        }

        return TextureAtlas().apply {
            textureRegions.forEach { (name, position) ->
                addRegion(name, TextureRegion(texture, position.x, position.y, textureSize, textureSize))
            }
        }
    }
}
