package dev.lucasnlm.antimine.gdx

import android.util.SizeF
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector2
import dev.lucasnlm.antimine.gdx.models.RenderSettings
import kotlin.math.absoluteValue

class CameraController(
    private val renderSettings: RenderSettings,
    private val camera: Camera,
) {
    private val velocity: Vector2 = Vector2.Zero.cpy()
    private var lockSpeed = false

    private fun limitSpeed(minefieldSize: SizeF) {
        val screenWidth = Gdx.graphics.width
        val screenHeight = Gdx.graphics.height
        val padding = renderSettings.internalPadding
        val virtualHeight = screenHeight - renderSettings.appBarHeight - renderSettings.navigationBarHeight

        camera.run {
            val newX = (position.x - velocity.x)
            val newY = (position.y + velocity.y)
            val start = 0.5f * screenWidth - padding.start
            val end = minefieldSize.width - 0.5f * screenWidth + padding.end
            val top = minefieldSize.height - 0.5f * screenHeight + padding.top + renderSettings.appBarHeight
            val bottom = 0.5f * screenHeight - padding.bottom - renderSettings.navigationBarHeight

            if (screenWidth > minefieldSize.width) {
                velocity.x = 0f
            } else {
                if (newX < start && velocity.x < 0.0) {
                    velocity.x = velocity.x.absoluteValue * 1f
                } else if (newX > end && velocity.x > 0.0) {
                    velocity.x = velocity.x.absoluteValue * -1f
                } else {
                    velocity.x *= 0.6f
                }
            }

            if (virtualHeight > minefieldSize.height) {
                velocity.y = 0f
            } else {
                if (newY > top && velocity.y > 0.0) {
                    velocity.y = velocity.y.absoluteValue * -0.15f
                } else if (newY < bottom && velocity.y < 0.0) {
                    velocity.y = velocity.y.absoluteValue * 0.15f
                } else {
                    velocity.y *= 0.6f
                }
            }
        }
    }

    fun act(minefieldSize: SizeF) {
        if (!velocity.isZero && !lockSpeed) {
            val delta = Gdx.graphics.deltaTime

            limitSpeed(minefieldSize)

            camera.run {
                translate(velocity.x * delta, velocity.y * delta, 0f)
                update(true)
                Gdx.graphics.requestRendering()
            }
        }
    }

    fun setLockSpeed(lockSpeed: Boolean) {
        this.lockSpeed = lockSpeed
    }

    fun translate(x: Float, y: Float) {
        camera.translate(x, y, 0f)
        camera.update(true)
    }

    fun addVelocity(dx: Float, dy: Float) {
        velocity.add(dx, dy)
    }
}
