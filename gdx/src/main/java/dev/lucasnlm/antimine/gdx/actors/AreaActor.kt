package dev.lucasnlm.antimine.gdx.actors

import android.view.ViewConfiguration
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.gdx.GdxLocal
import dev.lucasnlm.antimine.gdx.drawArea
import dev.lucasnlm.antimine.gdx.drawAsset
import dev.lucasnlm.antimine.gdx.models.TouchAreaAction
import dev.lucasnlm.antimine.gdx.scope
import dev.lucasnlm.antimine.gdx.toGdxColor
import dev.lucasnlm.antimine.gdx.toOppositeMax
import dev.lucasnlm.antimine.gdx.use
import dev.lucasnlm.antimine.gdx.useColor
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.model.minesAround

class AreaActor(
    size: Float,
    private var area: Area,
    private val theme: AppTheme,
    private val internalPadding: Float = Gdx.graphics.density * 1.5f,
    private val onSingleTouch: (Area) -> Unit,
    private val onLongTouch: (Area) -> Unit,
) : Actor() {

    init {
        width = size * Gdx.graphics.density
        height = size * Gdx.graphics.density
        x = area.posX * width
        y = area.posY * height

        touchable = if (area.isCovered || area.minesAround > 0) Touchable.enabled else Touchable.disabled

        addListener(object : InputListener() {
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                GdxLocal.pressedArea?.let {
                    if (!it.consumed && it.area.id == area.id) {
                        val dt = System.currentTimeMillis() - it.pressedAt

                        if (dt <= ViewConfiguration.getLongPressTimeout()) {
                            onSingleTouch(area)
                        }

                        GdxLocal.pressedArea = it.copy(consumed = true)
                    }
                }
                toBack()
            }

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                toFront()
                GdxLocal.pressedArea = TouchAreaAction(
                    area = area.copy(),
                    pressedAt = System.currentTimeMillis(),
                    consumed = false,
                    x = x,
                    y = y,
                )
                return true
            }
        })
    }

    fun bindArea(area: Area) {
        this.area = area
    }

    override fun act(delta: Float) {
        super.act(delta)

        GdxLocal.pressedArea?.let {
            if (it.area.id == area.id) {
                val dt = System.currentTimeMillis() - it.pressedAt

                if (!it.consumed) {
                    if (dt > ViewConfiguration.getLongPressTimeout()) {
                        onLongTouch(it.area)
                        GdxLocal.pressedArea = it.copy(consumed = true)
                    }
                }
            }
        }
    }

    private fun isCurrentlyPressed(): Boolean {
        return GdxLocal.pressedArea?.let {
            it.area.id == area.id
        } == true
    }

    override fun draw(unsafeBatch: Batch?, parentAlpha: Float) {
        super.draw(unsafeBatch, parentAlpha)
        val internalPadding = this.internalPadding

        val isCurrentTouch = isCurrentlyPressed()
        val areaAlpha = if (area.highlighted) 1.0f else GdxLocal.globalAlpha

        unsafeBatch?.scope { batch, textures ->
            val quality = GdxLocal.qualityZoomLevel
            val isOdd: Boolean

            val areaTexture = if (area.isCovered) {
                isOdd = if (area.posY % 2 == 0) {
                    area.posX % 2 != 0
                } else {
                    area.posX % 2 == 0
                }

                if (isOdd) textures.areaCoveredOdd[quality] else textures.areaCovered[quality]
            } else {
                isOdd = if (area.posY % 2 == 0) {
                    area.posX % 2 != 0
                } else {
                    area.posX % 2 == 0
                }

                if (isOdd) textures.areaUncoveredOdd[quality] else textures.areaUncovered[quality]
            }

            if (isCurrentTouch && area.isCovered && quality < 2 && GdxLocal.focusResizeLevel > 1.0f) {
                toFront()

                val resize = GdxLocal.focusResizeLevel
                val resizeShadow = GdxLocal.focusResizeLevel * 1.05f

                if (GdxLocal.focusResizeLevel > 1.0f) {
                    val alpha = (GdxLocal.focusResizeLevel - 1.0f).coerceAtLeast(0.0f)
                    batch.drawArea(
                        texture = if (isOdd) textures.detailedAreaOdd else textures.detailedArea,
                        x = x - width * (resizeShadow - 1.0f) * 0.5f,
                        y = y - height * (resizeShadow - 1.0f) * 0.5f,
                        width = width * resizeShadow,
                        height = height * resizeShadow,
                        color = Color(0f, 0f, 0f, alpha),
                        blend = true,
                    )
                }

                batch.drawArea(
                    texture = if (isOdd) textures.detailedAreaOdd else textures.detailedArea,
                    x = x - width * (resize - 1.0f) * 0.5f,
                    y = y - height * (resize - 1.0f) * 0.5f,
                    width = width * resize,
                    height = height * resize,
                    color = Color(1f, 1f, 1f, 1.0f),
                    blend = quality < 2,
                )
            } else {
                toBack()

                batch.drawArea(
                    texture = areaTexture,
                    x = x + internalPadding,
                    y = y + internalPadding,
                    width = width - internalPadding * 2,
                    height = height - internalPadding * 2,
                    color = Color(1f, 1f, 1f, areaAlpha),
                    blend = quality < 2,
                )
            }

            GdxLocal.gameTextures?.let {
                if (area.isCovered) {
                    when {
                        area.mark.isFlag() -> {
                            val color = theme.palette.covered.toOppositeMax(areaAlpha)
                            drawAsset(
                                batch = batch,
                                texture = it.flag,
                                color = color,
                                scale = if (isCurrentTouch) GdxLocal.focusResizeLevel else 1.0f
                            )
                        }
                        area.mark.isQuestion() -> {
                            val color = theme.palette.covered.toOppositeMax(areaAlpha)
                            drawAsset(
                                batch = batch,
                                texture = it.question,
                                color = color,
                                scale = if (isCurrentTouch) GdxLocal.focusResizeLevel else 1.0f
                            )
                        }
                    }
                } else {
                    if (area.minesAround > 0) {
                        drawAsset(
                            batch = batch,
                            texture = it.aroundMines[area.minesAround - 1],
                            color = theme.palette.minesAround(area.minesAround - 1).toGdxColor(areaAlpha),
                        )
                    }

                    if (area.hasMine) {
                        val color = if (isOdd) { theme.palette.uncoveredOdd } else { theme.palette.uncovered }
                        drawAsset(
                            batch = batch,
                            texture = it.mine,
                            color = color.toOppositeMax(areaAlpha),
                        )
                    }
                }
            }

            if (area.highlighted && !area.isCovered) {
                ShapeRenderer(64).use(ShapeRenderer.ShapeType.Line) {
                    projectionMatrix = batch.projectionMatrix
                    transformMatrix = batch.transformMatrix
                    useColor(theme.palette.highlight, 0.5f)
                    Gdx.gl.glLineWidth(Gdx.graphics.density * 2.0f)
                    rect(
                        x + internalPadding * 4,
                        y + internalPadding * 4,
                        width - internalPadding * 8,
                        height - internalPadding * 8,
                    )
                }
            }
        }
    }
}
