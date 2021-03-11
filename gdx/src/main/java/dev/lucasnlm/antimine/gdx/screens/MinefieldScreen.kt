package dev.lucasnlm.antimine.gdx.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import dev.lucasnlm.antimine.gdx.models.RenderSettings
import dev.lucasnlm.antimine.preferences.models.Minefield

class MinefieldScreen(
    private val renderSettings: RenderSettings,
) : Stage() {
    private var minefield: Minefield? = null
    private var minefieldWidth: Float? = null
    private var minefieldHeight: Float? = null

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
                val newY = (position.y - dy)
                val start = 0.5f * Gdx.graphics.width - renderSettings.internalPadding.start
                val end = minefieldWidth - 0.5f * Gdx.graphics.width + renderSettings.internalPadding.end
                val top = minefieldHeight - 0.5f * Gdx.graphics.height + renderSettings.internalPadding.top
                val bottom = 0.5f * Gdx.graphics.height - renderSettings.internalPadding.bottom

                if (width > minefieldWidth) {
                    dx = 0f
                } else {
                    if (newX < start) {
                        dx = newX - start
                    }
                    if (newX > end) {
                        dx = newX - end
                    }
                }

                if (height > minefieldHeight) {
                    dy = 0f
                } else {
                    if (newY > top) {
                        dy = top - newY
                    }

                    if (newY < bottom) {
                        dy = bottom - newY
                    }
                }

                translate(-dx, dy, 0f)
            } != null
        } else {
            false
        }
    }
}
