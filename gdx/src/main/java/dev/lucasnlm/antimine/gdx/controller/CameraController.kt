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

    private fun limitSpeed(minefieldSize: SizeF) {
        val screenWidth = Gdx.graphics.width
        val padding = renderSettings.internalPadding
        val invZoom = 1.0f / (camera as OrthographicCamera).zoom

        camera.run {
            val newX = (position.x - velocity.x)
            val newY = (position.y + velocity.y)
            val start = 0.25f * screenWidth - padding.start * invZoom
            val end = minefieldSize.width - 0.25f * screenWidth + padding.end * invZoom
            val top = 0.75f * minefieldSize.height + padding.top * invZoom
            val bottom = 0.25f * minefieldSize.height - padding.bottom * invZoom - renderSettings.navigationBarHeight

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

    fun addVelocity(dx: Float, dy: Float) {
        val zoom = (camera as OrthographicCamera).zoom
        velocity.add(dx * zoom, dy * zoom)
        camera.update(true)
    }

    companion object {
        const val RESISTANCE = 0.65f
    }
}
