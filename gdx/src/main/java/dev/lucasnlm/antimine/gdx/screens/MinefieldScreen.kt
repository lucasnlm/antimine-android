package dev.lucasnlm.antimine.gdx.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import dev.lucasnlm.antimine.gdx.BuildConfig
import dev.lucasnlm.antimine.gdx.GdxLocal
import dev.lucasnlm.antimine.gdx.models.RenderSettings
import dev.lucasnlm.antimine.preferences.models.Minefield

class MinefieldScreen(
    private val renderSettings: RenderSettings,
) : Stage() {
    private var minefield: Minefield? = null
    private var minefieldWidth: Float? = null
    private var minefieldHeight: Float? = null
    private var currentZoom: Float = 1.0f

    // Visibility references
    private var lastCameraPosition: Vector3? = null

    init {
        actionsRequestRendering = true

        addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return if (event?.target is Group) {
                    GdxLocal.hasHighlightAreas = false
                    event.cancel()
                    true
                } else {
                    false
                }
            }
        })
    }

    fun changeZoom(zoomMultiplier: Float) {
        (camera as OrthographicCamera).apply {
            zoom = (zoom * zoomMultiplier).coerceIn(1.0f, 4.0f)
            update(true)

            GdxLocal.qualityZoomLevel = (zoom.toInt() - 1).coerceAtLeast(0).coerceAtMost(2)
        }
        refreshVisibleActorsIfNeeded(true)
    }

    fun bindMinefield(minefield: Minefield) {
        this.minefield = minefield
        minefieldWidth = minefield.width * renderSettings.areaSize
        minefieldHeight = minefield.height * renderSettings.areaSize
        centerCamera()
    }

    private fun centerCamera() {
        val minefieldWidth = this.minefieldWidth
        val minefieldHeight = this.minefieldHeight
        if (minefieldWidth != null && minefieldHeight != null) {
            val virtualWidth = Gdx.graphics.width
            val virtualHeight = Gdx.graphics.height - renderSettings.navigationBarHeight

            val start = 0.5f * virtualWidth - renderSettings.internalPadding.start
            val end = minefieldWidth - 0.5f * virtualWidth + renderSettings.internalPadding.end
            val top = minefieldHeight - 0.5f * virtualHeight + renderSettings.internalPadding.top + renderSettings.appBarHeight
            val bottom = 0.5f * virtualHeight - renderSettings.internalPadding.bottom - renderSettings.navigationBarHeight

            camera.position.set((start + end) * 0.5f, (top + bottom) * 0.5f, 0f)
            camera.update(true)
        }
        refreshVisibleActorsIfNeeded()
        Gdx.graphics.requestRendering()
    }

    override fun act() {
        super.act()
        refreshVisibleActorsIfNeeded()

        val delta = Gdx.graphics.deltaTime

        if (GdxLocal.hasHighlightAreas) {
            GdxLocal.globalAlpha -= delta
            GdxLocal.globalAlpha = GdxLocal.globalAlpha.coerceAtLeast(0.45f)
        } else {
            GdxLocal.globalAlpha += delta * 2.0f
            GdxLocal.globalAlpha = GdxLocal.globalAlpha.coerceAtMost(1.0f)
        }

        GdxLocal.pressedArea?.let {
            if (!it.consumed && GdxLocal.focusResizeLevel < GdxLocal.maxFocusResizeLevel) {
                GdxLocal.focusResizeLevel =
                    (GdxLocal.focusResizeLevel + delta).coerceAtMost(GdxLocal.maxFocusResizeLevel)
            }

            if (it.consumed && GdxLocal.focusResizeLevel >= 1.0f) {
                GdxLocal.focusResizeLevel =
                    (GdxLocal.focusResizeLevel - delta * 0.2f).coerceAtLeast(1.0f)
            }
        }

        if (BuildConfig.DEBUG) {
            Gdx.app.log("GDX", "GDX Fps = ${Gdx.graphics.framesPerSecond}")
        }
    }

    private fun refreshVisibleActorsIfNeeded(forceRefresh: Boolean = false) {
        if (!camera.position.epsilonEquals(lastCameraPosition) || forceRefresh) {
            lastCameraPosition = camera.position.cpy()

            actors.forEach {
                it.isVisible = camera.frustum.boundsInFrustum(it.x, it.y, 0f, it.width, it.height, 0.0f)
            }
        }
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val minefieldWidth = this.minefieldWidth
        val minefieldHeight = this.minefieldHeight

        return if (minefieldWidth != null && minefieldHeight != null) {
            var dx = Gdx.input.deltaX.toFloat()
            var dy = Gdx.input.deltaY.toFloat()

            GdxLocal.pressedArea?.let {
                GdxLocal.pressedArea = it.copy(consumed = true)
            }

            val virtualWidth = Gdx.graphics.width
            val virtualHeight = Gdx.graphics.height

            camera?.run {
                val newX = (position.x - dx)
                val newY = (position.y + dy)
                val start = 0.5f * virtualWidth - renderSettings.internalPadding.start
                val end = minefieldWidth - 0.5f * virtualWidth + renderSettings.internalPadding.end
                val top = minefieldHeight - 0.5f * virtualHeight + renderSettings.internalPadding.top + renderSettings.appBarHeight
                val bottom = 0.5f * virtualHeight - renderSettings.internalPadding.bottom - renderSettings.navigationBarHeight

                if (virtualWidth > minefieldWidth) {
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

                if (virtualHeight > minefieldHeight) {
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
                update(true)
                refreshVisibleActorsIfNeeded()
            } != null
        } else {
            false
        }
    }
}
