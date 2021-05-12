package dev.lucasnlm.antimine.gdx.controller
import android.util.SizeF
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import dev.lucasnlm.antimine.gdx.models.RenderSettings

class CameraController(
    private val renderSettings: RenderSettings,
    private val camera: Camera,
) {
    private val velocity: Vector2 = Vector2.Zero.cpy()
    private var touch: Vector2? = null
    private var unlockTouch = false

    private fun limitSpeed(minefieldSize: SizeF) {
        val padding = renderSettings.internalPadding
        val zoom = (camera as OrthographicCamera).zoom
        val screenWidth = if (zoom < 1.0f) Gdx.graphics.width * zoom else Gdx.graphics.width.toFloat()
        val invZoom = 1.0f / zoom

        camera.run {
            val newX = (position.x - velocity.x)
            val newY = (position.y + velocity.y)
            val start = 0.25f * screenWidth - padding.start * invZoom
            val end = minefieldSize.width - 0.25f * screenWidth + padding.end * invZoom
            val top = minefieldSize.height + padding.top * invZoom
            val bottom = padding.bottom * invZoom - renderSettings.navigationBarHeight

            if ((newX < start && velocity.x < 0.0) || (newX > end && velocity.x > 0.0)) {
                velocity.x = 0.0f
            } else {
                velocity.x *= RESISTANCE
            }

            if ((newY > top && velocity.y > 0.0) || newY < bottom && velocity.y < 0.0) {
                velocity.y = 0.0f
            } else {
                velocity.y *= RESISTANCE
            }
        }
    }

    fun act(minefieldSize: SizeF) {
        if (velocity.len2() > 0.01f) {
            limitSpeed(minefieldSize)

            camera.run {
                translate(velocity.x, velocity.y, 0f)
                update(true)
                Gdx.graphics.requestRendering()
            }
        }
    }

    fun freeTouch(x: Float, y: Float) {
        touch?.let {
            val dx = it.x - x
            val dy = y - it.y
            velocity.add(dx * Gdx.graphics.deltaTime, dy * Gdx.graphics.deltaTime)
        }
        touch = null
        unlockTouch = false
    }

    fun startTouch(x: Float, y: Float) {
        if (touch == null) {
            touch = Vector2(x, y)
        }
    }

    fun translate(dx: Float, dy: Float, x: Float, y: Float) {
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

    companion object {
        const val RESISTANCE = 0.65f
    }
}
