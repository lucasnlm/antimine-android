package dev.lucasnlm.antimine.gdx.actors

import android.view.ViewConfiguration
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
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
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.model.minesAround

class AreaActor(
    size: Float,
    private var area: Area,
    private var areaForm: AreaForm,
    private var previousForm: AreaForm? = null,
    private val radiusLevel: Float,
    private val theme: AppTheme,
    private val internalPadding: Float = 0f,
    private val onSingleTouch: (Area) -> Unit,
    private val onLongTouch: (Area) -> Unit,
    private var coverAlpha: Float = 1.0f,
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
                GdxLocal.apply {
                    highlightAlpha = 0.45f
                    pressedArea = TouchAreaAction(
                        area = area.copy(),
                        pressedAt = System.currentTimeMillis(),
                        consumed = false,
                        x = x,
                        y = y,
                    )
                }
                return true
            }
        })
    }

    fun boundAreaId() = area.id

    fun bindArea(area: Area, areaForm: AreaForm) {
        if (area.isCovered) {
            if (areaForm != previousForm && area.mark == this.area.mark) {
                previousForm = this.areaForm
            }

            this.areaForm = areaForm
            this.coverAlpha = 1.0f
        } else if (area.isCovered != this.area.isCovered) {
            this.coverAlpha = 1.0f
        }

        this.area = area
    }

    override fun act(delta: Float) {
        super.act(delta)

        if (!area.isCovered && coverAlpha > 0.0f) {
            coverAlpha = (coverAlpha - delta * 3.0f).coerceAtLeast(0.0f)
            Gdx.graphics.requestRendering()
        } else if (previousForm != null && coverAlpha > 0.0f) {
            coverAlpha = (coverAlpha - delta * 3.0f).coerceAtLeast(0.0f)
            Gdx.graphics.requestRendering()

            if (coverAlpha == 0.0f) {
                previousForm = null
                coverAlpha = 1.0f
            }
        }

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
        val isCurrentTouch = isCurrentlyPressed()

        unsafeBatch?.scope { batch, textures ->
            val quality = 0
            val isOdd: Boolean = if (area.posY % 2 == 0) { area.posX % 2 != 0 } else { area.posX % 2 == 0 }

            if (isOdd && GdxLocal.qualityZoomLevel < 2) {
                textures.areaTextures[AreaForm.Full]?.let {
                    batch.drawArea(
                        texture = it,
                        x = x + internalPadding,
                        y = y + internalPadding,
                        width = width - internalPadding * 2,
                        height = height - internalPadding * 2,
                        color = Color(1.0f, 1.0f, 1.0f, 0.1f),
                        blend = quality < 2,
                    )
                }
            }

            if (isCurrentTouch && area.isCovered && quality < 2 && GdxLocal.focusResizeLevel > 1.0f) {
                val resize = GdxLocal.focusResizeLevel

                if (area.isCovered) {
                    textures.areaTextures[areaForm]?.let {
                        batch.drawArea(
                            texture = it,
                            x = x + internalPadding,
                            y = y + internalPadding,
                            width = width - internalPadding * 2,
                            height = height - internalPadding * 2,
                            color = Color(1.0f, 1.0f, 1.0f, coverAlpha),
                            blend = quality < 2,
                        )
                    }
                }

                textures.areaTextures[AreaForm.Full]?.let {
                    val baseColor = if (isOdd) theme.palette.coveredOdd else theme.palette.covered
                    val touchColor = baseColor.toOppositeMax(coverAlpha).mul(0.8f, 0.8f, 0.8f, 1.0f)
                    batch.drawArea(
                        texture = it,
                        x = x - width * (resize - 1.0f) * 0.5f,
                        y = y - height * (resize - 1.0f) * 0.5f,
                        width = width * resize,
                        height = height * resize,
                        color = touchColor,
                        blend = quality < 2,
                    )
                }
            } else {
                if (coverAlpha > 0.0f) {
                    previousForm?.let {  areaForm ->
                        textures.areaTextures[areaForm]?.let {
                            batch.drawArea(
                                texture = it,
                                x = x + internalPadding,
                                y = y + internalPadding,
                                width = width - internalPadding * 2,
                                height = height - internalPadding * 2,
                                color = if (area.mark.isNotNone()) {
                                    Color(0.5f, 0.5f, 0.5f, coverAlpha)
                                } else {
                                    Color(1.0f, 1.0f, 1.0f, coverAlpha)
                                },
                                blend = quality < 2,
                            )
                        }
                    }

                    val coverAlpha = if (previousForm != null) 1.0f else coverAlpha
                    textures.areaTextures[areaForm]?.let {
                        batch.drawArea(
                            texture = it,
                            x = x + internalPadding,
                            y = y + internalPadding,
                            width = width - internalPadding * 2,
                            height = height - internalPadding * 2,
                            color = if (area.mark.isNotNone()) {
                                Color(0.5f, 0.5f, 0.5f, coverAlpha)
                            } else {
                                Color(1.0f, 1.0f, 1.0f, coverAlpha)
                            },
                            blend = quality < 2,
                        )
                    }
                }

                if (area.hasMine && !area.isCovered) {
                    textures.areaTextures[AreaForm.Full]?.let {
                        batch.drawArea(
                            texture = it,
                            x = x + internalPadding,
                            y = y + internalPadding,
                            width = width - internalPadding * 2,
                            height = height - internalPadding * 2,
                            color = Color(1.0f, 0f, 0f, 0.45f),
                            blend = quality < 2,
                        )
                    }
                } else if (area.isCovered && isOdd && theme.palette.covered != theme.palette.coveredOdd && area.mark.isNone()) {
                    if (GdxLocal.qualityZoomLevel < 2) {
                        textures.areaTextures[AreaForm.Full]?.let {
                            batch.drawArea(
                                texture = it,
                                x = x + internalPadding,
                                y = y + internalPadding,
                                width = width - internalPadding * 2,
                                height = height - internalPadding * 2,
                                color = theme.palette.coveredOdd.toGdxColor(0.1f),
                                blend = quality < 2,
                            )
                        }
                    }
                }
            }

            if (!area.isCovered && GdxLocal.pressedArea?.area?.id == area.id) {
                textures.areaTextures[AreaForm.Full]?.let {
                    val density = Gdx.graphics.density
                    batch.drawArea(
                        texture = it,
                        x = x + internalPadding + density * 2f,
                        y = y + internalPadding + density * 2f,
                        width = width - internalPadding * 2f - density * 4f,
                        height = height - internalPadding * 2f - density * 4f,
                        color = theme.palette.highlight.toGdxColor(GdxLocal.highlightAlpha),
                        blend = quality < 2,
                    )
                }
            }

            GdxLocal.gameTextures?.let {
                if (area.isCovered) {
                    when {
                        area.mark.isFlag() -> {
                            val color = theme.palette.covered.toOppositeMax(1.0f)
                            drawAsset(
                                batch = batch,
                                texture = it.flag,
                                color = color,
                                scale = if (isCurrentTouch) GdxLocal.focusResizeLevel else 1.0f
                            )
                        }
                        area.mark.isQuestion() -> {
                            val color = theme.palette.covered.toOppositeMax(1.0f)
                            drawAsset(
                                batch = batch,
                                texture = it.question,
                                color = color,
                                scale = if (isCurrentTouch) GdxLocal.focusResizeLevel else 1.0f
                            )
                        }
                        area.revealed -> {
                            val color = if (isOdd) { theme.palette.uncoveredOdd } else { theme.palette.uncovered }
                            drawAsset(
                                batch = batch,
                                texture = it.mine,
                                color = color.toGdxColor(0.65f),
                            )
                        }
                    }
                } else {
                    if (area.minesAround > 0 && GdxLocal.qualityZoomLevel < 2) {
                        drawAsset(
                            batch = batch,
                            texture = it.aroundMines[area.minesAround - 1],
                            color = theme.palette.minesAround(area.minesAround - 1).toGdxColor(1.0f),
                        )
                    }

                    if (area.hasMine) {
                        val color = if (isOdd) { theme.palette.uncoveredOdd } else { theme.palette.uncovered }
                        drawAsset(
                            batch = batch,
                            texture = it.mine,
                            color = color.toOppositeMax(1.0f),
                        )
                    }
                }
            }
        }
    }

    companion object {
        private fun Area.canMargeWith(area: Area): Boolean {
            return !isCovered || mark.mergeId != area.mark.mergeId
        }

        fun getForm(area: Area, field: List<Area>): AreaForm {
            val top = field.getPos(area.posX, area.posY + 1)?.run { canMargeWith(area) } ?: true
            val bottom = field.getPos(area.posX, area.posY - 1)?.run { canMargeWith(area) } ?: true
            val left = field.getPos(area.posX - 1, area.posY)?.run { canMargeWith(area) } ?: true
            val right = field.getPos(area.posX + 1, area.posY)?.run { canMargeWith(area) } ?: true

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
