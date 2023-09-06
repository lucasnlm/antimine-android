package dev.lucasnlm.antimine.gdx.controller
import android.util.SizeF
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import dev.lucasnlm.antimine.gdx.models.RenderSettings

class CameraController(
    private val renderSettings: RenderSettings,
    private val camera: Camera,
) {
    private var lastCameraPosition: Vector3? = null
    private var touch: Vector2? = null
    private var unlockTouch = false

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
                val padding = renderSettings.internalPadding
                val zoom = (camera as OrthographicCamera).zoom
                val screenWidth = if (zoom < 1.0f) Gdx.graphics.width * zoom else Gdx.graphics.width.toFloat()
                val screenHeight = if (zoom < 1.0f) Gdx.graphics.height * zoom else Gdx.graphics.height.toFloat()
                val invZoom = 1.0f / zoom
                val percentLimit = 0.15f

                val start = percentLimit * screenWidth - padding.start * invZoom
                val end = minefieldSize.width - percentLimit * screenWidth + padding.end * invZoom
                val top = minefieldSize.height + padding.top * invZoom - percentLimit * screenHeight
                val bottom = padding.bottom * invZoom - renderSettings.navigationBarHeight + percentLimit * screenHeight

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
            if (Vector2(x, y).sub(it.x, it.y).len() > renderSettings.areaSize || unlockTouch) {
                camera.run {
                    translate(dx, dy, 0f)
                    update(true)
                    Gdx.graphics.requestRendering()
                }
                unlockTouch = true
            }
        }
    }
}
