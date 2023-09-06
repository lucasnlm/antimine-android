package dev.lucasnlm.antimine.gdx

import com.badlogic.gdx.utils.viewport.FitViewport
import kotlin.math.floor

class PixelPerfectViewport(worldWidth: Float, worldHeight: Float) : FitViewport(worldWidth, worldHeight) {
    override fun update(
        screenWidth: Int,
        screenHeight: Int,
        centerCamera: Boolean,
    ) {
        val wRate = screenWidth / worldWidth
        val hRate = screenHeight / worldHeight
        val rate = wRate.coerceAtMost(hRate)

        val iRate = 1f.coerceAtLeast(floor(rate))

        val viewportWidth = worldWidth.toInt() * iRate
        val viewportHeight = worldHeight.toInt() * iRate

        setScreenBounds(
            ((screenWidth - viewportWidth) * 0.5f).toInt(),
            ((screenHeight - viewportHeight) * 0.5f).toInt(),
            viewportWidth.toInt(),
            viewportHeight.toInt(),
        )
        apply(false)
    }
}
