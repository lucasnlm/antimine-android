package dev.lucasnlm.antimine.gdx.controller
import android.util.SizeF
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import dev.lucasnlm.antimine.gdx.GameContext
import dev.lucasnlm.antimine.gdx.models.GameRenderingContext
import dev.lucasnlm.antimine.gdx.stages.MinefieldStage

class CameraController(
    private val gameRenderingContext: GameRenderingContext,
    private val camera: OrthographicCamera,
) {
    private var lastCameraPosition: Vector3? = null
    private var touch: Vector2? = null
    private var unlockTouch: Boolean = false
    private var currentZoom: Float = 1.0f

    private fun limitValueBetween(
        value: Float,
        min: Float,
        max: Float,
        default: Float?,
    ): Float {
        return if (min < max) {
            value.coerceIn(min, max)
        } else {
            // No need to limit.
            default ?: value
        }
    }

    fun act(minefieldSize: SizeF) {
        val cameraPosition = camera.position
        if (lastCameraPosition != cameraPosition) {
            camera.run {
                val padding = gameRenderingContext.internalPadding
                val zoom = camera.zoom
                val screenWidth = if (zoom < 1.0f) Gdx.graphics.width * zoom else Gdx.graphics.width.toFloat()
                val screenHeight = if (zoom < 1.0f) Gdx.graphics.height * zoom else Gdx.graphics.height.toFloat()
                val invZoom = 1.0f / zoom
                val percentLimit = 0.15f

                val navigationBarHeight = gameRenderingContext.navigationBarHeight
                val start = percentLimit * screenWidth - padding.start * invZoom
                val end = minefieldSize.width - percentLimit * screenWidth + padding.end * invZoom
                val top = minefieldSize.height + padding.top * invZoom - percentLimit * screenHeight
                val bottom = padding.bottom * invZoom - navigationBarHeight + percentLimit * screenHeight

                val limitedX = limitValueBetween(camera.position.x, start, end, lastCameraPosition?.x)
                val limitedY = limitValueBetween(camera.position.y, bottom, top, lastCameraPosition?.y)

                lastCameraPosition = Vector3(limitedX, limitedY, 0.0f)
                camera.position.set(lastCameraPosition)

                update(true)
                Gdx.graphics.requestRendering()
            }
        }
    }

    fun freeTouch() {
        touch = null
        unlockTouch = false
    }

    fun startTouch(
        x: Float,
        y: Float,
    ) {
        touch = touch ?: Vector2(x, y)
    }

    fun translate(
        dx: Float,
        dy: Float,
        x: Float,
        y: Float,
    ) {
        touch?.let {
            if (Vector2(x, y).sub(it.x, it.y).len() > gameRenderingContext.areaSize || unlockTouch) {
                camera.run {
                    translate(dx * currentZoom, dy * currentZoom, 0f)
                    update(true)
                }
                unlockTouch = true
            }
        }
    }

    fun setZoom(value: Float) {
        camera.apply {
            zoom = value.coerceIn(0.8f, 3.0f)
            currentZoom = zoom
            update(true)

            GameContext.zoomLevelAlpha =
                when {
                    zoom < 3.5f -> {
                        1.0f
                    }
                    zoom > 4.0f -> {
                        0.0f
                    }
                    else -> {
                        (3.5f - zoom)
                    }
                }
        }
    }

    fun scaleZoom(zoomMultiplier: Float) {
        camera.apply {
            val newZoom =
                if (zoomMultiplier > 1.0) {
                    zoom + 1.0f * Gdx.graphics.deltaTime
                } else {
                    zoom - 1.0f * Gdx.graphics.deltaTime
                }
            zoom = newZoom.coerceIn(MinefieldStage.MAX_ZOOM_OUT, MinefieldStage.MAX_ZOOM_IN)
            if (currentZoom != zoom) {
                currentZoom = zoom
                Gdx.graphics.requestRendering()
            }

            GameContext.zoomLevelAlpha =
                when {
                    zoom < 3.5f -> {
                        1.0f
                    }
                    zoom > 4.0f -> {
                        0.0f
                    }
                    else -> {
                        (3.5f - zoom)
                    }
                }
        }
    }

    fun centerCameraTo(minefieldSize: SizeF) {
        val virtualWidth = Gdx.graphics.width
        val virtualHeight = Gdx.graphics.height
        val padding = gameRenderingContext.internalPadding

        val start = 0.5f * virtualWidth - padding.start
        val end = minefieldSize.width - 0.5f * virtualWidth + padding.end
        val top = minefieldSize.height - 0.5f * (virtualHeight - padding.top)
        val bottom = 0.5f * virtualHeight + padding.bottom - gameRenderingContext.navigationBarHeight

        camera.run {
            position.set((start + end) * 0.5f, (top + bottom) * 0.5f, 0f)
            update(true)
        }

        Gdx.graphics.requestRendering()
    }
}
