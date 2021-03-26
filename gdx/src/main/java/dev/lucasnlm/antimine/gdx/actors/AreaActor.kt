package dev.lucasnlm.antimine.gdx.actors

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
import dev.lucasnlm.antimine.gdx.events.GameEvent
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
    private var coverAlpha: Float = 1.0f,
    private var isPressed: Boolean = false,
    private val theme: AppTheme,
    private val squareDivider: Float = 0f,
    private val onInputEvent: (GameEvent) -> Unit,
) : Actor() {

    init {
        width = size
        height = size
        x = area.posX * width
        y = area.posY * height

        addListener(object : InputListener() {
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                onInputEvent(GameEvent.TouchUpEvent(area.id))
                isPressed = false
                toBack()
                Gdx.graphics.requestRendering()
            }

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                toFront()
                GdxLocal.highlightAlpha = 0.45f
                isPressed = true
                onInputEvent(GameEvent.TouchDownEvent(area.id))
                Gdx.graphics.requestRendering()
                return true
            }
        })
    }

    fun boundAreaId() = area.id

    fun bindArea(reset: Boolean, area: Area, areaForm: AreaForm) {
        if (reset) {
            this.isPressed = false
            this.areaForm = areaForm
            this.previousForm = null
            this.coverAlpha = 1.0f
        } else {
            if (area.isCovered) {
                if (areaForm != previousForm && area.mark == this.area.mark) {
                    previousForm = this.areaForm
                }

                this.areaForm = areaForm
                this.coverAlpha = 1.0f
            } else if (area.isCovered != this.area.isCovered) {
                this.coverAlpha = 1.0f
            }
        }

        this.area = area
        touchable = if (area.isCovered || area.minesAround > 0) Touchable.enabled else Touchable.disabled
    }

    override fun act(delta: Float) {
        super.act(delta)

        if (!area.isCovered && coverAlpha > 0.0f) {
            val revealDelta = delta * 4.0f * GdxLocal.animationScale
            coverAlpha = (coverAlpha - revealDelta).coerceAtLeast(0.0f)
            Gdx.graphics.requestRendering()
        } else if (previousForm != null && coverAlpha > 0.0f) {
            val revealDelta = delta * 4.0f * GdxLocal.animationScale
            coverAlpha = (coverAlpha - revealDelta).coerceAtLeast(0.0f)

            if (coverAlpha == 0.0f) {
                previousForm = null
                coverAlpha = 1.0f
            }

            Gdx.graphics.requestRendering()
        }
    }

    override fun draw(unsafeBatch: Batch?, parentAlpha: Float) {
        super.draw(unsafeBatch, parentAlpha)

        val internalPadding = squareDivider
        val isAboveOthers = isPressed

        unsafeBatch?.scope { batch, textures ->
            val quality = 0
            val isOdd: Boolean = if (area.posY % 2 == 0) { area.posX % 2 != 0 } else { area.posX % 2 == 0 }

            if (!isOdd && !area.isCovered && GdxLocal.qualityZoomLevel < 2) {
                textures.areaTextures[AreaForm.None]?.let {
                    batch.drawArea(
                        texture = it,
                        x = x + internalPadding,
                        y = y + internalPadding,
                        width = width - internalPadding * 2,
                        height = height - internalPadding * 2,
                        color = theme.palette.background.toGdxColor().mul(0.5f, 0.5f, 0.5f, 0.1f),
                        blend = true,
                    )
                }
            }

            if (isAboveOthers && area.isCovered && quality < 2 && GdxLocal.focusResizeLevel > 1.0f) {
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
                    batch.drawArea(
                        texture = it,
                        x = x - width * (resize - 1.0f) * 0.5f,
                        y = y - height * (resize - 1.0f) * 0.5f,
                        width = width * resize,
                        height = height * resize,
                        color = Color(0.65f, 0.65f, 0.65f, 1.0f),
                        blend = true,
                    )
                }
            } else {
                if (coverAlpha > 0.0f) {
                    previousForm?.let { areaForm ->
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

            if (!area.isCovered && isPressed) {
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
                                scale = if (isAboveOthers) GdxLocal.focusResizeLevel else 1.0f
                            )
                        }
                        area.mark.isQuestion() -> {
                            val color = theme.palette.covered.toOppositeMax(1.0f)
                            drawAsset(
                                batch = batch,
                                texture = it.question,
                                color = color,
                                scale = if (isAboveOthers) GdxLocal.focusResizeLevel else 1.0f
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
