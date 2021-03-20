package dev.lucasnlm.antimine.gdx.screens

import android.util.SizeF
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.gdx.BuildConfig
import dev.lucasnlm.antimine.gdx.CameraController
import dev.lucasnlm.antimine.gdx.GdxLocal
import dev.lucasnlm.antimine.gdx.actors.AreaActor
import dev.lucasnlm.antimine.gdx.actors.AreaForm
import dev.lucasnlm.antimine.gdx.models.RenderSettings
import dev.lucasnlm.antimine.preferences.models.Minefield

class MinefieldScreen(
    private val renderSettings: RenderSettings,
    private val onSingleTouch: (Area) -> Unit,
    private val onLongTouch: (Area) -> Unit,
    forceFreeScroll: Boolean,
) : Stage() {
    private var minefield: Minefield? = null
    private var minefieldSize: SizeF? = null
    private var currentZoom: Float = 1.0f
    private var lastCameraPosition: Vector3? = null

    private val cameraController: CameraController

    private var boundHashCode: Int? = null
    private var boundAreas = listOf<Area>()

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

        cameraController = CameraController(
            camera = camera,
            renderSettings = renderSettings,
            forceFreeScroll = forceFreeScroll,
        )
    }

    fun changeZoom(zoomMultiplier: Float) {
        (camera as OrthographicCamera).apply {
            zoom = (zoom * zoomMultiplier).coerceIn(0.5f, 4.0f)
            update(true)

            GdxLocal.qualityZoomLevel = (zoom.toInt() - 1).coerceAtLeast(0).coerceAtMost(2)
        }
        refreshVisibleActorsIfNeeded(true)
    }

    fun bindField(field: List<Area>) {
        boundAreas = field
    }

    private fun refreshAreas() {
        val currentHashCode = boundAreas.hashCode()
        if (boundAreas.hashCode() != boundHashCode) {
            boundHashCode = currentHashCode

            boundAreas.let { field ->
                if (actors.size != field.size) {
                    clear()
                    field.map {
                        AreaActor(
                            theme = renderSettings.theme,
                            size = renderSettings.areaSize,
                            area = it,
                            areaForm = if (it.isCovered) AreaActor.getForm(it, field) else AreaForm.None,
                            onSingleTouch = onSingleTouch,
                            onLongTouch = onLongTouch,
                        )
                    }.forEach {
                        addActor(it)
                    }
                } else {
                    actors.forEach {
                        val areaActor = (it as AreaActor)
                        val area = field[areaActor.boundAreaId()]
                        areaActor.bindArea(area, AreaActor.getForm(area, field))
                    }
                }
            }

            Gdx.graphics.requestRendering()
        }
    }

    fun bindSize(newMinefield: Minefield?) {
        minefield = newMinefield
        minefieldSize = newMinefield?.let {
            SizeF(
                it.width * renderSettings.areaSize,
                it.height * renderSettings.areaSize,
            )
        }
        centerCamera()
    }

    private fun centerCamera() {
        this.minefieldSize?.let {
            val virtualWidth = Gdx.graphics.width
            val virtualHeight = Gdx.graphics.height
            val padding = renderSettings.internalPadding

            val start = 0.5f * virtualWidth - padding.start
            val end = it.width - 0.5f * virtualWidth + padding.end
            val top = it.height - 0.5f * (virtualHeight + padding.top - renderSettings.appBarHeight)
            val bottom = 0.5f * virtualHeight + padding.bottom - renderSettings.navigationBarHeight

            camera.run {
                position.set((start + end) * 0.5f, (top + bottom) * 0.5f, 0f)
                update(true)
            }
        }
        refreshVisibleActorsIfNeeded()
        Gdx.graphics.requestRendering()
    }

    override fun act() {
        super.act()

        // Handle camera movement
        minefieldSize?.let { cameraController.act(it) }

        refreshAreas()
        refreshVisibleActorsIfNeeded()

        val delta = Gdx.graphics.deltaTime

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
        return minefieldSize?.let {
            val dx = Gdx.input.deltaX.toFloat()
            val dy = Gdx.input.deltaY.toFloat()

            if (dx * dx + dy * dy > 16f) {
                GdxLocal.pressedArea = null
            }

            cameraController.addVelocity(
                -dx * currentZoom,
                dy * currentZoom,
            )
            true
        } != null
    }
}
