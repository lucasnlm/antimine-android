package dev.lucasnlm.antimine.gdx.actors

import android.view.ViewConfiguration
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import dev.lucasnlm.antimine.core.getPos
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
    private var areaForm: AreaForm,
    private val theme: AppTheme,
    private val internalPadding: Float = 0f,
    private val onSingleTouch: (Area) -> Unit,
    private val onLongTouch: (Area) -> Unit,
) : Actor() {

    init {
        width = size
        height = size
        x = area.posX * width
        y = area.posY * height

        touchable = if (area.isCovered || area.minesAround > 0) Touchable.enabled else Touchable.disabled

        addListener(object : InputListener() {
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                GdxLocal.pressedArea?.let {
                    if (!it.consumed && it.area.id == area.id) {
                        onSingleTouch(it.area)
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

    fun boundAreaId() = area.id

    fun bindArea(area: Area, areaForm: AreaForm) {
        this.area = area
        this.areaForm = areaForm
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
        return GdxLocal.pressedArea?.let { it.area.id == area.id } == true
    }

    override fun draw(unsafeBatch: Batch?, parentAlpha: Float) {
        super.draw(unsafeBatch, parentAlpha)
        val internalPadding = this.internalPadding

        if (GdxLocal.qualityZoomLevel > 0 && !area.isCovered) {
            return
        }

        val isCurrentTouch = isCurrentlyPressed()
        val areaAlpha = if (area.highlighted) 1.0f else GdxLocal.globalAlpha

        unsafeBatch?.scope { batch, textures ->
            val quality = 0
            val isOdd: Boolean = if (area.posY % 2 == 0) { area.posX % 2 != 0 } else { area.posX % 2 == 0 }

            val areaTexture: Texture? = if (area.isCovered) {
                textures.areaTextures[areaForm]
            } else {
                null
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

                areaTexture?.let {
                    batch.drawArea(
                        texture = it,
                        x = x + internalPadding,
                        y = y + internalPadding,
                        width = width - internalPadding * 2,
                        height = height - internalPadding * 2,
                        color = Color(1f, 1f, 1f, areaAlpha),
                        blend = quality < 2,
                    )
                }

                batch.drawArea(
                    texture = textures.detailedAreaOdd,
                    x = x - width * (resize - 1.0f) * 0.5f,
                    y = y - height * (resize - 1.0f) * 0.5f,
                    width = width * resize,
                    height = height * resize,
                    color = Color(1f, 1f, 1f, 1.0f),
                    blend = quality < 2,
                )
            } else {
                toBack()

                if (!area.isCovered) {
                    batch.drawArea(
                        texture = if (isOdd) textures.areaUncoveredOdd[quality] else textures.areaUncovered[quality],
                        x = x + internalPadding,
                        y = y + internalPadding,
                        width = width - internalPadding * 2,
                        height = height - internalPadding * 2,
                        color = Color(1f, 1f, 1f, 0.25f),
                        blend = quality < 2,
                    )
                }

                areaTexture?.let {
                    batch.drawArea(
                        texture = it,
                        x = x + internalPadding,
                        y = y + internalPadding,
                        width = width - internalPadding * 2,
                        height = height - internalPadding * 2,
                        color = Color(1f, 1f, 1f, areaAlpha),
                        blend = quality < 2,
                    )
                }

                if (area.isCovered && isOdd) {
                    batch.drawArea(
                        texture = textures.areaCoveredOdd[quality],
                        x = x + internalPadding,
                        y = y + internalPadding,
                        width = width - internalPadding * 2,
                        height = height - internalPadding * 2,
                        color = Color(1f, 1f, 1f, 0.25f),
                        blend = quality < 2,
                    )
                }

                if (area.hasMine && !area.isCovered) {
                    batch.drawArea(
                        texture = textures.areaCoveredOdd[quality],
                        x = x + internalPadding,
                        y = y + internalPadding,
                        width = width - internalPadding * 2,
                        height = height - internalPadding * 2,
                        color = Color(1f, 1f, 1f, 0.25f),
                        blend = quality < 2,
                    )
                }
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

    companion object {
        fun getForm(area: Area, field: List<Area>): AreaForm {
            val top = field.getPos(area.posX, area.posY + 1)?.run { !isCovered || mark != area.mark } ?: true
            val bottom = field.getPos(area.posX, area.posY - 1)?.run { !isCovered || mark != area.mark } ?: true
            val left = field.getPos(area.posX - 1, area.posY)?.run { !isCovered || mark != area.mark } ?: true
            val right = field.getPos(area.posX + 1, area.posY)?.run { !isCovered || mark != area.mark } ?: true

            var roundCorners = 0b0000

            if (top && left) {
                roundCorners = roundCorners or 0b1000
            }
            if (top && right) {
                roundCorners = roundCorners or 0b0100
            }
            if (bottom && left) {
                roundCorners = roundCorners or 0b0010
            }
            if (bottom && right) {
                roundCorners = roundCorners or 0b0001
            }

            return when (roundCorners) {
                0b1000 -> AreaForm.LeftTop
                0b0100 -> AreaForm.RightTop
                0b0101 -> AreaForm.FullRight
                0b1100 -> AreaForm.FullTop
                0b1010 -> AreaForm.FullLeft
                0b0010 -> AreaForm.LeftBottom
                0b0001 -> AreaForm.RightBottom
                0b0011 -> AreaForm.FullBottom
                0b1111 -> AreaForm.Full
                else -> AreaForm.None
            }
        }
    }
}
