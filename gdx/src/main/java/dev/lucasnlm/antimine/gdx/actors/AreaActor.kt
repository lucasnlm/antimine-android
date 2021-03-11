package dev.lucasnlm.antimine.gdx.actors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Actor
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.gdx.BuildConfig

class AreaActor(
    val size: Float,
    private val area: Area,
    private val internalPadding: Float = 2f,
) : Actor() {
    private val shapeRenderer by lazy {
        ShapeRenderer()
    }

    init {
        width = size
        height = size
        //debug = BuildConfig.DEBUG
        x = area.posX * width
        y = area.posY * height
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        batch?.scope {
            val color = Color.RED
            shapeRenderer.projectionMatrix = batch.projectionMatrix
            shapeRenderer.setColor(color.r, color.g, color.b, color.a * parentAlpha)

//            Gdx.gl.glEnable(GL20.GL_BLEND)
//            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

            shapeRenderer.scope {
                //transformMatrix.setTranslation(internalPadding, internalPadding, 0f)
                roundedRect(
                    x = x + internalPadding,
                    y = y + internalPadding,
                    width = width - internalPadding * 2,
                    height = height - internalPadding * 2,
                    radius = 5f
                )
            }

//            Gdx.gl.glDisable(GL20.GL_BLEND)
//            Gdx.gl.glLineWidth(1f)
//            shapeRenderer.setColor(Color.WHITE)
        }

    }
}
