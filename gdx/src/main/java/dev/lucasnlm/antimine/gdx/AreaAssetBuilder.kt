package dev.lucasnlm.antimine.gdx

import android.graphics.Color
import android.util.Size
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.ImageResolver
import com.badlogic.gdx.math.GridPoint2
import com.badlogic.gdx.math.Vector
import com.badlogic.gdx.math.Vector2
import dev.lucasnlm.antimine.gdx.actors.AreaForm
import dev.lucasnlm.antimine.gdx.actors.areaNoForm
import dev.lucasnlm.antimine.ui.ext.alpha
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

        val border = (Gdx.graphics.density * 10f).toInt()
        val square = size - border * 2
        val radius = (square * 0.5 * radiusLevel * 0.1f).toInt()

        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888).apply {
            blending = Pixmap.Blending.None
            filter = Pixmap.Filter.BiLinear

            setColor(Color.WHITE.toGdxColor(0.0f))
            fillRectangle(0, 0, size, size)

            setColor(Color.WHITE.toGdxColor(1.0f))

            if (areaForm == areaNoForm) {
                fillRectangle(0, 0, size, size)
            } else {
                val realRadius = (size * 0.5f).toInt()
                //fillCircle(realRadius, realRadius, realRadius)

                fillRectangle(border, border + radius, square + 1, square - (radius * 2) + 1)
                fillRectangle(border + radius, border, square - (radius * 2), square + 1)

                areaForm.run {
                    if (!top && !left) {
                        fillCircle(border + radius, border + radius, radius)
                    }

                    if (!top && !right) {
                        fillCircle(border + square - radius, border + radius, radius)
                    }

                    if (!bottom && !right) {
                        fillCircle(border + square - radius, border + square - radius, radius)
                    }

                    if (!bottom && !left) {
                        fillCircle(border + radius, border + square - radius, radius)
                    }

                    if (top && right) {
                        setColor(Color.WHITE.toGdxColor(1.0f))
                        fillRectangle(border + square, 0, border, border)
                        setColor(Color.WHITE.toGdxColor(0.0f))
                        fillCircle(border + square + border, 0, border)
                    }

                    if (top && left) {
                        setColor(Color.WHITE.toGdxColor(1.0f))
                        fillRectangle(0, 0, border, border)
                        setColor(Color.WHITE.toGdxColor(0.0f))
                        fillCircle(0, 0, border)
                    }

                    if (bottom && right) {
                        setColor(Color.WHITE.toGdxColor(1.0f))
                        fillRectangle(border + square, border + square, border, border)
                        setColor(Color.WHITE.toGdxColor(0.0f))
                        fillCircle(border + square + border, border + square + border, border)
                    }

                    if (bottom && left) {
                        setColor(Color.WHITE.toGdxColor(1.0f))
                        fillRectangle(0, border + square, border, border)
                        setColor(Color.WHITE.toGdxColor(0.0f))
                        fillCircle(0, border + square + border, border)
                    }

                    if (bottom) {
                        setColor(Color.RED.toGdxColor(1.0f))
                        fillRectangle(border, border + square / 2, square + 1, square / 2 + border)
                    }

                    if (top) {
                        setColor(Color.GREEN.toGdxColor(1.0f))
                        fillRectangle(border, 0, square + 1, size / 2 + border)
                    }

                    if (right) {
                        setColor(Color.YELLOW.toGdxColor(1.0f))
                        fillRectangle(square / 2 + border, border, square / 2 + border, square + 1)
                    }

                    if (left) {
                        setColor(Color.BLUE.toGdxColor(1.0f))
                        fillRectangle(0, border, square / 2 + border, square + 1)
                    }
                }
            }
        }

        return Texture(pixmap).also {
            pixmap.dispose()
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }

    fun getAreaTextureAtlas(
        radiusLevel: Int,
    ): TextureAtlas {
        val pixMapSize = 2048
        val textureSize = 2048 / 8
        val coverColor = Color.WHITE.toGdxColor(1.0f)
        val transparent = Color.WHITE.toGdxColor(0.0f)

        val border = (textureSize * 0.075f).toInt()
        val square = textureSize - border * 2
        val radius = (square * 0.5 * radiusLevel * 0.1f).toInt()

        var x = 0
        var y = 0

        val textureRegions = mutableMapOf<String, GridPoint2>()

        val pixmap = Pixmap(pixMapSize, pixMapSize, Pixmap.Format.RGBA8888).apply {
            blending = Pixmap.Blending.None
            filter = Pixmap.Filter.NearestNeighbour

            setColor(transparent)
            fillRectangle(0, 0, pixMapSize, pixMapSize)

            // core
            x = 0
            y = 0
            textureRegions["core"] = GridPoint2(0, 0)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillRectangle(x + border, y + border + radius, square, square - (radius * 2))
            fillRectangle(x + border + radius, y + border, square - (radius * 2), square)

            // bottom
            x = textureSize
            y = 0
            textureRegions["bottom"] = GridPoint2(x, y)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillRectangle(x + border, y + textureSize / 2, square, textureSize / 2)

            // top
            x = textureSize * 2
            y = 0
            textureRegions["top"] = GridPoint2(x, y)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillRectangle(x + border, y, square, textureSize / 2)

            // right
            x = textureSize * 3
            y = 0
            textureRegions["right"] = GridPoint2(x, y)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillRectangle(x + square / 2 + border, y + border, square / 2 + border, square)

            // left
            x = textureSize * 4
            y = 0
            textureRegions["left"] = GridPoint2(x, y)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillRectangle(x, y + border, square / 2 + border, square)

            // corner top left
            x = textureSize * 5
            y = 0
            textureRegions["corner-top-left"] = GridPoint2(x, y)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillCircle(x + border + radius, y + border + radius, radius)

            // corner top right
            x = textureSize * 6
            y = 0
            textureRegions["corner-top-right"] = GridPoint2(x, y)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillCircle(x + textureSize - radius - border - 1, y + border + radius, radius)

            // corner bottom right
            x = textureSize * 7
            y = 0
            textureRegions["corner-bottom-right"] = GridPoint2(x, y)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillCircle(x + textureSize - radius - border - 1,  y + textureSize - radius - border - 1, radius)

            // corner bottom left
            x = 0
            y = textureSize
            textureRegions["corner-bottom-left"] = GridPoint2(x, y)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillCircle(x + border + radius,  y + textureSize - radius - border - 1, radius)

            // border corner top right
            x = textureSize
            y = textureSize
            textureRegions["bc-top-right"] = GridPoint2(x, y)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillRectangle(x + border + square, y, border, border)
            setColor(transparent)
            fillCircle(x + border + square + border, y, border)

            // border corner top left
            x = textureSize * 2
            y = textureSize
            textureRegions["bc-top-left"] = GridPoint2(x, y)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillRectangle(x, y, border, border)
            setColor(transparent)
            fillCircle(x, y, border)

            // border corner bottom right
            x = textureSize * 3
            y = textureSize
            textureRegions["bc-bottom-right"] = GridPoint2(x, y)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillRectangle(x + border + square, y + border + square, border, border)
            setColor(transparent)
            fillCircle(x + border + square + border, y + border + square + border, border)

            // border corner bottom left
            x = textureSize * 4
            y = textureSize
            textureRegions["bc-bottom-left"] = GridPoint2(x, y)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillRectangle(x, y + border + square, border, border)
            setColor(transparent)
            fillCircle(x, y + border + square + border, border)

            setColor(coverColor)

            // top, top-left, left
            x = textureSize * 7
            y = textureSize
            textureRegions["t-l-tl"] = GridPoint2(x, y)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillRectangle(x, y, border, border)

            // top, top-right, right
            x = textureSize * 6
            y = textureSize
            textureRegions["t-r-tr"] = GridPoint2(x, y)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillRectangle(x + textureSize - border, y, border, border)

            // bottom, bottom-right, right
            x = 0
            y = textureSize * 2
            textureRegions["b-r-br"] = GridPoint2(x, y)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillRectangle(x + textureSize - border, y + textureSize - border, border, border)

            // bottom, bottom-left, left
            x = textureSize
            y = textureSize * 2
            textureRegions["b-l-bl"] = GridPoint2(x, y)
            setColor(transparent)
            fillRectangle(x, y, textureSize, textureSize)
            setColor(coverColor)
            fillRectangle(x, y + textureSize - border, border, border)
        }

        val texture = Texture(pixmap).apply {
            setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        }

        pixmap.dispose()

        return TextureAtlas().apply {
            textureRegions.forEach { (name, position) ->
                addRegion(name, TextureRegion(texture, position.x, position.y, textureSize, textureSize))
            }
        }
    }
}
