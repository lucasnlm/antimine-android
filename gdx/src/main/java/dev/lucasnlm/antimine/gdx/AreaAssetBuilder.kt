package dev.lucasnlm.antimine.gdx

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow

object AreaAssetBuilder {
    fun getAreaTexture(
        expectedSize: Float,
        radiusLevel: Int,
        qualityLevel: Int,
        backgroundColor: Int,
        color: Int,
        alphaEnabled: Boolean = false
    ): Texture {
        val initialSize = expectedSize.toDouble() / (qualityLevel.coerceAtLeast(0) * 2 + 1)
        val size = initialSize.run {
            2.0.pow(ceil(ln(this) / ln(2.0)))
        }.toInt()

        val radius = ((size * 0.5) * (radiusLevel * 0.1)).toInt()
        val format = if (alphaEnabled) Pixmap.Format.RGBA8888 else Pixmap.Format.RGB888

        val pixmap = Pixmap(size, size, format).apply {
            blending = Pixmap.Blending.SourceOver
            filter = Pixmap.Filter.BiLinear
            if (!alphaEnabled) {
                setColor(backgroundColor.toGdxColor())
                fillRectangle(0, 0, size, size)
            }
            setColor(color.toGdxColor())
            fillRectangle(0, radius, size, size - radius * 2)
            fillRectangle(radius, 0, size - radius * 2, size)
            fillCircle(radius, radius, radius)
            fillCircle(radius, size - radius, radius)
            fillCircle(size - radius, radius, radius)
            fillCircle(size - radius, size - radius, radius)
        }

        return Texture(pixmap).also {
            pixmap.dispose()
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }
}
