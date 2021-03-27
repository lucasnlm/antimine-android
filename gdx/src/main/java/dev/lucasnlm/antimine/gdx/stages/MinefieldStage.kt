package dev.lucasnlm.antimine.gdx.stages

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
import dev.lucasnlm.antimine.gdx.controller.CameraController
import dev.lucasnlm.antimine.gdx.GdxLocal
import dev.lucasnlm.antimine.gdx.actors.AreaActor
import dev.lucasnlm.antimine.gdx.actors.AreaForm
import dev.lucasnlm.antimine.gdx.events.GameEvent
import dev.lucasnlm.antimine.gdx.models.ActionSettings
import dev.lucasnlm.antimine.gdx.models.RenderSettings
import dev.lucasnlm.antimine.preferences.models.Minefield

class MinefieldStage(
    private val actionSettings: ActionSettings,
    private val renderSettings: RenderSettings,
    private val onSingleTap: (Int) -> Unit,
    private val onDoubleTap: (Int) -> Unit,
    private val onLongTouch: (Int) -> Unit,
) : Stage() {
    private var minefield: Minefield? = null
    private var minefieldSize: SizeF? = null
    private var currentZoom: Float = 1.0f

    private var lastCameraPosition: Vector3? = null
    private var lastZoom: Float? = null

    private val cameraController: CameraController

    private var boundHashCode: Int? = null
    private var boundAreas = listOf<Area>()

    private var inputInit: Long = 0L
    private val inputEvents: MutableList<GameEvent> = mutableListOf()

    init {
        actionsRequestRendering = true

        addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return if (event?.target is Group) {
                    GdxLocal.apply {
                        hasHighlightAreas = false
                        highlightAlpha = 0.0f
                    }
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
        )
    }

    fun changeZoom(zoomMultiplier: Float) {
        (camera as OrthographicCamera).apply {
            val newZoom = if (zoomMultiplier > 1.0) {
                zoom + 2.0f * zoomMultiplier * Gdx.graphics.deltaTime
            } else {
                zoom - 3.0f * (1.0f / zoomMultiplier) * Gdx.graphics.deltaTime
            }
            zoom = newZoom.coerceIn(0.9f, 4.0f)
            update(true)

            GdxLocal.qualityZoomLevel = (zoom.toInt() - 1).coerceAtLeast(0).coerceAtMost(2)
        }

        inputEvents.clear()
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
                            areaForm = when {
                                renderSettings.joinAreas ->
                                    if (it.isCovered) AreaActor.getForm(it, field) else AreaForm.None
                                else -> AreaForm.Full
                            },
                            onInputEvent = ::handleGameEvent,
                            squareDivider = renderSettings.squareDivider,
                        )
                    }.forEach {
                        addActor(it)
                    }
                } else {
                    val reset = field.count { it.hasMine } == 0

                    actors.forEach {
                        val areaActor = (it as AreaActor)
                        val area = field[areaActor.boundAreaId()]
                        val areaForm = if (renderSettings.joinAreas) AreaActor.getForm(area, field) else AreaForm.Full
                        areaActor.bindArea(reset, area, areaForm)
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
            val top = it.height - 0.5f * (virtualHeight + padding.top - renderSettings.appBarWithStatusHeight)
            val bottom = 0.5f * virtualHeight + padding.bottom - renderSettings.navigationBarHeight

            camera.run {
                position.set((start + end) * 0.5f, (top + bottom) * 0.5f, 0f)
                update(true)
            }
        }
        Gdx.graphics.requestRendering()
    }

    private fun handleGameEvent(gameEvent: GameEvent) {
        if (inputEvents.firstOrNull { it.id != gameEvent.id } != null) {
            inputEvents.clear()
        }

        if (inputEvents.firstOrNull { it is GameEvent.TouchUpEvent } == null) {
            inputInit = System.currentTimeMillis()
        }

        if (gameEvent is GameEvent.TouchUpEvent && inputEvents.firstOrNull { it is GameEvent.TouchDownEvent } == null) {
            // Ignore unpaired up event
            return
        }

        inputEvents.add(gameEvent)
    }

    private fun checkGameInput(now: Long) {
        if (inputEvents.isNotEmpty()) {
            val dt = now - inputInit

            val touchUpEvents = inputEvents.filterIsInstance<GameEvent.TouchUpEvent>()
            val touchDownEvents = inputEvents.filterIsInstance<GameEvent.TouchDownEvent>()

            if (touchUpEvents.isNotEmpty()) {
                if (touchUpEvents.size == touchDownEvents.size) {
                    if (actionSettings.handleDoubleTaps) {
                        if (dt > actionSettings.doubleTapTimeout) {
                            touchUpEvents.groupBy { it.id }
                                .entries
                                .first()
                                .let {
                                    when (it.value.count()) {
                                        1 -> onSingleTap(it.key)
                                        2 -> onDoubleTap(it.key)
                                        else -> {
                                        }
                                    }
                                }.also {
                                    inputEvents.clear()
                                    Gdx.graphics.requestRendering()
                                }
                        }
                    } else {
                        touchUpEvents.map { it.id }
                            .first()
                            .run(onSingleTap)
                            .also {
                                inputEvents.clear()
                                Gdx.graphics.requestRendering()
                            }
                    }
                }
            } else if (touchDownEvents.isNotEmpty()) {
                if (dt > actionSettings.longTapTimeout) {
                    touchDownEvents.map { it.id }
                        .first()
                        .run(onLongTouch)
                        .also {
                            inputEvents.clear()
                            Gdx.graphics.requestRendering()
                        }
                }
            }
        }
    }

    override fun act() {
        super.act()

        checkGameInput(System.currentTimeMillis())

        // Handle camera movement
        minefieldSize?.let { cameraController.act(it) }

        refreshAreas()

        refreshVisibleActorsIfNeeded()

        GdxLocal.run {
            if (highlightAlpha > 0.0f) {
                highlightAlpha = (highlightAlpha - 0.25f * Gdx.graphics.deltaTime).coerceAtLeast(0.0f)
                Gdx.graphics.requestRendering()
            }
        }

        if (BuildConfig.DEBUG) {
            Gdx.app.log("GDX", "GDX FPS = ${Gdx.graphics.framesPerSecond}")
        }
    }

    private fun refreshVisibleActorsIfNeeded() {
        val camera = camera as OrthographicCamera
        if (!camera.position.epsilonEquals(lastCameraPosition) || lastZoom != camera.zoom) {
            lastCameraPosition = camera.position.cpy()
            lastZoom = camera.zoom

            actors.forEach {
                it.isVisible = camera.frustum.boundsInFrustum(it.x, it.y, 0f, it.width, it.height, 0.0f)
            }
        }

        if (BuildConfig.DEBUG) {
            val visibleCount = actors.count { it.isVisible }
            Gdx.app.log("GDX", "GDX count = $visibleCount")
        }
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return minefieldSize?.let {
            val dx = Gdx.input.deltaX.toFloat()
            val dy = Gdx.input.deltaY.toFloat()

            if (dx * dx + dy * dy > 100f) {
                inputEvents.clear()
            }

            cameraController.addVelocity(
                -dx * currentZoom,
                dy * currentZoom,
            )
            true
        } != null
    }
}
