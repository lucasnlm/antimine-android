package dev.lucasnlm.antimine.gdx.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener
import dev.lucasnlm.antimine.gdx.models.RenderSettings
import dev.lucasnlm.antimine.preferences.models.Minefield


class MinefieldScreen(
    private val renderSettings: RenderSettings,
) : Stage() {
    private var minefield: Minefield? = null
    private var minefieldWidth: Float? = null
    private var minefieldHeight: Float? = null
    private var currentZoom: Float = 1.0f

    init {
        addListener(object : ActorGestureListener() {
            override fun pinch(
                event: InputEvent?,
                initialPointer1: Vector2?,
                initialPointer2: Vector2?,
                pointer1: Vector2?,
                pointer2: Vector2?
            ) {
                super.pinch(event, initialPointer1, initialPointer2, pointer1, pointer2)
                print("zoom = pintch")
            }

            override fun zoom(event: InputEvent?, initialDistance: Float, distance: Float) {
                super.zoom(event, initialDistance, distance)
                print("zoom = ${initialDistance / distance}")
            }
        })
    }

    fun changeZoom(zoomMultiplier: Float) {
        (camera as OrthographicCamera).apply {
            zoom = (zoom * zoomMultiplier).coerceIn(1.0f, 4.0f)
        }
    }

    fun bindMinefield(minefield: Minefield) {
        this.minefield = minefield
        println("minefield = $minefield")
        minefieldWidth = minefield.width * renderSettings.areaSize
        minefieldHeight = minefield.height * renderSettings.areaSize
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val minefieldWidth = this.minefieldWidth
        val minefieldHeight = this.minefieldHeight

        return if (minefieldWidth != null && minefieldHeight != null) {
            camera?.run {
                var dx = Gdx.input.deltaX.toFloat()
                var dy = Gdx.input.deltaY.toFloat()
                val newX = (position.x - dx)
                val newY = (position.y + dy)
                val start = 0.5f * Gdx.graphics.width - renderSettings.internalPadding.start
                val end = minefieldWidth - 0.5f * Gdx.graphics.width + renderSettings.internalPadding.end
                val top = minefieldHeight - 0.5f * Gdx.graphics.height + renderSettings.internalPadding.top
                val bottom = 0.5f * Gdx.graphics.height - renderSettings.internalPadding.bottom

                if (width > minefieldWidth) {
                    dx = 0f
                } else {
                    if (newX < start) {
                        dx = 0f
                        position.set(start, position.y, 0f)
                    }
                    if (newX > end) {
                        dx = 0f
                        position.set(end, position.y, 0f)
                    }
                }

                if (height > minefieldHeight) {
                    dy = 0f
                } else {
                    if (newY > top) {
                        dy = 0f
                        position.set(position.x, top, 0f)
                    }

                    if (newY < bottom) {
                        dy = 0f
                        position.set(position.x, bottom, 0f)
                    }
                }

                translate(-dx * currentZoom, dy * currentZoom, 0f)
            } != null
        } else {
            false
        }
    }
}
