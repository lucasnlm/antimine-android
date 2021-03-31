package dev.lucasnlm.antimine.gdx

import android.graphics.Color
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import dev.lucasnlm.antimine.gdx.actors.AreaForm
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow

object AreaAssetBuilder {
    fun getAreaTexture(
        expectedSize: Float,
        radiusLevel: Int,
    ): Texture {
        val size = expectedSize.toDouble().run {
            2.0.pow(ceil(ln(this) / ln(2.0)))
        }.toInt()

        val radius = (size * 0.5 * radiusLevel * 0.1f).toInt()

        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888).apply {
            blending = Pixmap.Blending.None
            filter = Pixmap.Filter.BiLinear

            setColor(Color.WHITE.toGdxColor(0.0f))
            fillRectangle(0, 0, size, size)

            setColor(Color.WHITE.toGdxColor(1.0f))
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

    fun getAreaBorderTexture(
        expectedSize: Float,
        radiusLevel: Int,
    ): Texture {
        val size = expectedSize.toDouble().run {
            2.0.pow(ceil(ln(this) / ln(2.0)))
        }.toInt()

        val radius = (size * 0.5 * radiusLevel * 0.1f).toInt()
        val border = (3 * Gdx.graphics.density).toInt()

        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888).apply {
            blending = Pixmap.Blending.None
            filter = Pixmap.Filter.BiLinear

            setColor(Color.WHITE.toGdxColor(0.0f))
            fillRectangle(0, 0, size, size)

            setColor(Color.WHITE)
            fillRectangle(0, radius, size, size - radius * 2)
            fillRectangle(radius, 0, size - radius * 2, size)
            fillCircle(radius, radius, radius)
            fillCircle(radius, size - radius, radius)
            fillCircle(size - radius, radius, radius)
            fillCircle(size - radius, size - radius, radius)

            val newSize = size - border * 2
            val newRadius = (newSize * 0.5 * radiusLevel * 0.1f).toInt()

            setColor(Color.WHITE.toGdxColor(0.0f))
            fillRectangle(border, border + newRadius, newSize, newSize - newRadius * 2)
            fillRectangle(border + newRadius, border, newSize - newRadius * 2, newSize)
            fillCircle(border + newRadius, border + newRadius, newRadius)
            fillCircle(border + newRadius, border + newSize - newRadius, newRadius)
            fillCircle(border + newSize - newRadius, border + newRadius, newRadius)
            fillCircle(border + newSize - newRadius, border + newSize - newRadius, newRadius)
        }

        return Texture(pixmap).also {
            pixmap.dispose()
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }

    fun getAreaTextureForm(
        areaForm: AreaForm,
        expectedSize: Float,
        radiusLevel: Int,
    ): Texture {
        val size = expectedSize.toDouble().run {
            2.0.pow(ceil(ln(this) / ln(2.0)))
        }.toInt()

        val radius = (size * 0.5 * radiusLevel * 0.1f).toInt()

        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888).apply {
            blending = Pixmap.Blending.None
            filter = Pixmap.Filter.BiLinear

            setColor(Color.WHITE.toGdxColor(0.0f))
            fillRectangle(0, 0, size, size)

            setColor(Color.WHITE.toGdxColor(1.0f))

            if (areaForm == AreaForm.None) {
                fillRectangle(0, 0, size, size)
            } else {
                val realRadius = (size * 0.5f).toInt()
                fillCircle(realRadius, realRadius, realRadius)

                when (areaForm) {
                    AreaForm.LeftTop -> {
                        fillCircle(radius, radius, radius)
                        fillRectangle(radius, -radius, realRadius - radius, realRadius)
                        fillRectangle(0, radius, realRadius, realRadius - radius)
                        fillRectangle(0, size - realRadius, width, realRadius)
                        fillRectangle(size - realRadius, 0, realRadius, height)
                    }
                    AreaForm.RightTop -> {
                        fillCircle(size - radius, radius, radius)
                        fillRectangle(0, 0, size - radius, size)
                        fillRectangle(realRadius, radius, size - radius, size)
                    }
                    AreaForm.FullTop -> {
                        fillRectangle(0, radius, size, size - radius * 2)
                        fillRectangle(radius, 0, size - radius * 2, size)
                        fillCircle(radius, radius, radius)
                        fillCircle(size - radius, radius, radius)
                        fillRectangle(0, realRadius, size, realRadius)
                    }
                    AreaForm.Full -> {
                        fillRectangle(0, radius, size, size - radius * 2)
                        fillRectangle(radius, 0, size - radius * 2, size)
                        fillCircle(radius, radius, radius)
                        fillCircle(radius, size - radius, radius)
                        fillCircle(size - radius, radius, radius)
                        fillCircle(size - radius, size - radius, radius)
                    }
                    AreaForm.RightBottom -> {
                        fillRectangle(0, radius, size, size - radius * 2)
                        fillRectangle(radius, 0, size - radius * 2, size)
                        fillCircle(size - radius, size - radius, radius)
                        fillRectangle(0, 0, size, realRadius)
                        fillRectangle(0, 0, realRadius, size)
                    }
                    AreaForm.FullRight -> {
                        fillRectangle(0, radius, size, size - radius * 2)
                        fillRectangle(radius, 0, size - radius * 2, size)
                        fillCircle(size - radius, radius, radius)
                        fillCircle(size - radius, size - radius, radius)
                        fillRectangle(0, 0, realRadius, size)
                    }
                    AreaForm.FullLeft -> {
                        fillRectangle(0, radius, size, size - radius * 2)
                        fillRectangle(radius, 0, size - radius * 2, size)
                        fillCircle(radius, radius, radius)
                        fillCircle(radius, size - radius, radius)
                        fillRectangle(size - realRadius, 0, realRadius, size)
                    }
                    AreaForm.FullBottom -> {
                        fillRectangle(0, radius, size, size - radius * 2)
                        fillRectangle(radius, 0, size - radius * 2, size)
                        fillCircle(radius, size - radius, radius)
                        fillCircle(size - radius, size - radius, radius)
                        fillRectangle(0, 0, size, realRadius)
                    }
                    AreaForm.LeftBottom -> {
                        fillRectangle(0, radius, size, size - radius * 2)
                        fillRectangle(radius, 0, size - radius * 2, size)
                        fillCircle(radius, size - radius, radius)
                        fillRectangle(0, 0, size, realRadius)
                        fillRectangle(size - realRadius, 0, realRadius, size)
                    }
                    else -> {
                        fillRectangle(0, 0, size, size)
                    }
                }
            }
        }

        return Texture(pixmap).also {
            pixmap.dispose()
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }
}
