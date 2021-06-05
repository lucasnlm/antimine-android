package dev.lucasnlm.antimine.gdx.actors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import dev.lucasnlm.antimine.core.getPos
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.gdx.GdxLocal
import dev.lucasnlm.antimine.gdx.alpha
import dev.lucasnlm.antimine.gdx.dim
import dev.lucasnlm.antimine.gdx.drawAsset
import dev.lucasnlm.antimine.gdx.drawRegion
import dev.lucasnlm.antimine.gdx.drawTexture
import dev.lucasnlm.antimine.gdx.events.GdxEvent
import dev.lucasnlm.antimine.gdx.toGdxColor
import dev.lucasnlm.antimine.gdx.toOppositeMax
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.model.minesAround

class AreaActor(
    size: Float,
    field: List<Area>,
    enableLigatures: Boolean,
    private var area: Area,
    private var focusScale: Float = 1.0f,
    private var isPressed: Boolean = false,
    private val pieces: MutableList<String> = ArrayList(20),
    private val theme: AppTheme,
    private val onInputEvent: (GdxEvent) -> Unit,
) : Actor() {

    private var areaForm: AreaForm? = null

    private val topId: Int
    private val bottomId: Int
    private val leftId: Int
    private val rightId: Int
    private val topLeftId: Int
    private val topRightId: Int
    private val bottomLeftId: Int
    private val bottomRightId: Int

    init {
        width = size
        height = size
        x = area.posX * width
        y = area.posY * height

        addListener(object : InputListener() {
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                onInputEvent(GdxEvent.TouchUpEvent(area.id))
                isPressed = false
                toBack()
                Gdx.graphics.requestRendering()
            }

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                toFront()
                GdxLocal.highlightAlpha = 0.45f
                isPressed = true
                onInputEvent(GdxEvent.TouchDownEvent(area.id))
                Gdx.graphics.requestRendering()
                return true
            }
        })

        topId = field.getPos(area.posX, area.posY + 1)?.id ?: -1
        bottomId = field.getPos(area.posX, area.posY - 1)?.id ?: -1
        leftId = field.getPos(area.posX - 1, area.posY)?.id ?: -1
        rightId = field.getPos(area.posX + 1, area.posY)?.id ?: -1
        topLeftId = field.getPos(area.posX - 1, area.posY + 1)?.id ?: -1
        topRightId = field.getPos(area.posX + 1, area.posY + 1)?.id ?: -1
        bottomLeftId = field.getPos(area.posX - 1, area.posY - 1)?.id ?: -1
        bottomRightId = field.getPos(area.posX + 1, area.posY - 1)?.id ?: -1

        bindArea(reset = true, ligatureEnabled = enableLigatures, area = area, field = field)
    }

    fun boundAreaId() = area.id

    fun boundAreaHashCode() = area.hashCode()

    fun bindArea(reset: Boolean, ligatureEnabled: Boolean, area: Area, field: List<Area>) {
        if (reset) {
            this.isPressed = false
        }

        val newForm = when {
            area.isCovered && ligatureEnabled -> {
                AreaForm(
                    top = field.getOrNull(topId)?.canLigatureTo(area) == true,
                    bottom = field.getOrNull(bottomId)?.canLigatureTo(area) == true,
                    left = field.getOrNull(leftId)?.canLigatureTo(area) == true,
                    right = field.getOrNull(rightId)?.canLigatureTo(area) == true,
                    topLeft = field.getOrNull(topLeftId)?.canLigatureTo(area) == true,
                    topRight = field.getOrNull(topRightId)?.canLigatureTo(area) == true,
                    bottomLeft = field.getOrNull(bottomLeftId)?.canLigatureTo(area) == true,
                    bottomRight = field.getOrNull(bottomRightId)?.canLigatureTo(area) == true,
                )
            }
            else -> {
                areaNoForm
            }
        }

        if ((area.isCovered || area.hasMine) && this.areaForm != newForm) {
            this.areaForm = newForm

            pieces.clear()
            val newPieces = mapOf(
                FormNames.core to true,
                FormNames.top to newForm.top,
                FormNames.left to newForm.left,
                FormNames.bottom to newForm.bottom,
                FormNames.right to newForm.right,
                FormNames.cornerTopLeft to (!newForm.top && !newForm.left),
                FormNames.cornerTopRight to (!newForm.top && !newForm.right),
                FormNames.cornerBottomLeft to (!newForm.bottom && !newForm.left),
                FormNames.cornerBottomRight to (!newForm.bottom && !newForm.right),
                FormNames.borderCornerTopRight to (newForm.top && newForm.right && !newForm.topRight),
                FormNames.borderCornerTopLeft to (newForm.top && newForm.left && !newForm.topLeft),
                FormNames.borderCornerBottomRight to (newForm.bottom && newForm.right && !newForm.bottomRight),
                FormNames.borderCornerBottomLeft to (newForm.bottom && newForm.left && !newForm.bottomLeft),
                FormNames.fillTopLeft to (newForm.top && newForm.left && newForm.topLeft),
                FormNames.fillTopRight to (newForm.top && newForm.right && newForm.topRight),
                FormNames.fillBottomLeft to (newForm.bottom && newForm.left && newForm.bottomLeft),
                FormNames.fillBottomRight to (newForm.bottom && newForm.right && newForm.bottomRight),
            ).filter {
                it.value
            }.keys
            pieces.addAll(newPieces)
        }

        this.area = area
    }

    override fun act(delta: Float) {
        super.act(delta)
        val area = this.area

        touchable = if ((area.isCovered || area.minesAround > 0) && GdxLocal.actionsEnabled)
            Touchable.enabled else Touchable.disabled

        val isCurrentFocus = GdxLocal.currentFocus?.id == area.id
        val isEnterPressed = Gdx.input.isKeyPressed(Input.Keys.ENTER) || Gdx.input.isKeyPressed(Input.Keys.DPAD_CENTER)

        if (isCurrentFocus && touchable == Touchable.enabled) {
            if (zIndex != Int.MAX_VALUE) {
                toFront()
            }

            if (isEnterPressed && !isPressed) {
                onInputEvent(GdxEvent.TouchDownEvent(area.id))
                isPressed = true
            } else if (!isEnterPressed && isPressed) {
                toBack()
                onInputEvent(GdxEvent.TouchUpEvent(area.id))
                isPressed = false
            }
        }

        focusScale = if (isPressed) {
            (focusScale + Gdx.graphics.deltaTime).coerceAtMost(MAX_SCALE)
        } else {
            (focusScale - Gdx.graphics.deltaTime).coerceAtLeast(MIN_SCALE)
        }
    }

    private fun drawBackground(batch: Batch, isOdd: Boolean) {
        if (!isOdd && !area.isCovered && GdxLocal.zoomLevelAlpha > 0.0f) {
            GdxLocal.gameTextures?.areaCovered?.let {
                batch.drawTexture(
                    texture = it,
                    x = x,
                    y = y,
                    width = width,
                    height = height,
                    blend = false,
                    color = theme.palette.background.toOppositeMax(GdxLocal.zoomLevelAlpha).alpha(0.025f)
                )
            }
        }
    }

    private fun drawCovered(batch: Batch, isOdd: Boolean) {
        val coverColor = when {
            area.mark.isNotNone() -> theme.palette.covered
            isOdd -> theme.palette.coveredOdd
            else -> theme.palette.covered
        }

        GdxLocal.areaAtlas?.let { atlas ->
            if (areaForm == areaFullForm) {
                batch.drawRegion(
                    texture = atlas.findRegion(FormNames.full),
                    x = x - Gdx.graphics.density * 2,
                    y = y - Gdx.graphics.density * 2,
                    width = width + 4 * Gdx.graphics.density,
                    height = height + 4 * Gdx.graphics.density,
                    color = if (area.mark.isNotNone()) {
                        coverColor.toGdxColor(1.0f).dim(0.6f)
                    } else {
                        coverColor.toGdxColor(1.0f)
                    },
                    blend = false,
                )
            } else {
                pieces.forEach { piece ->
                    batch.drawRegion(
                        texture = atlas.findRegion(piece),
                        x = x - Gdx.graphics.density * 2,
                        y = y - Gdx.graphics.density * 2,
                        width = width + 4 * Gdx.graphics.density,
                        height = height + 4 * Gdx.graphics.density,
                        color = if (area.mark.isNotNone()) {
                            coverColor.toGdxColor(1.0f).dim(0.6f)
                        } else {
                            coverColor.toGdxColor(1.0f)
                        },
                        blend = false,
                    )
                }
            }
        }
    }

    private fun drawMineBackground(batch: Batch) {
        val coverColor = Color(0.8f, 0.3f, 0.3f, 1.0f)

        GdxLocal.areaAtlas?.let { atlas ->
            pieces.forEach { piece ->
                batch.drawRegion(
                    texture = atlas.findRegion(piece),
                    x = x - Gdx.graphics.density * 2,
                    y = y - Gdx.graphics.density * 2,
                    width = width + 4 * Gdx.graphics.density,
                    height = height + 4 * Gdx.graphics.density,
                    color = coverColor,
                    blend = false,
                )
            }
        }
    }

    private fun drawCoveredIcons(batch: Batch) {
        val isAboveOthers = isPressed
        GdxLocal.gameTextures?.let {
            when {
                area.mark.isFlag() -> {
                    val color = if (area.mistake) {
                        Color(0.8f, 0.3f, 0.3f, 1.0f)
                    } else {
                        theme.palette.covered.toOppositeMax(0.8f)
                    }

                    drawAsset(
                        batch = batch,
                        texture = it.flag,
                        color = color,
                        scale = if (isAboveOthers) focusScale else BASE_ICON_SCALE
                    )
                }
                area.mark.isQuestion() -> {
                    val color = theme.palette.covered.toOppositeMax(1.0f)
                    drawAsset(
                        batch = batch,
                        texture = it.question,
                        color = color,
                        scale = if (isAboveOthers) focusScale else BASE_ICON_SCALE
                    )
                }
                area.revealed -> {
                    val color = theme.palette.uncovered
                    drawAsset(
                        batch = batch,
                        texture = it.mine,
                        color = color.toGdxColor(0.65f),
                        scale = BASE_ICON_SCALE
                    )
                }
            }
        }
    }

    private fun drawUncoveredIcons(batch: Batch) {
        GdxLocal.gameTextures?.let {
            if (area.minesAround > 0) {
                drawAsset(
                    batch = batch,
                    texture = it.aroundMines[area.minesAround - 1],
                    color = theme.palette.minesAround(area.minesAround - 1).toGdxColor(GdxLocal.zoomLevelAlpha),
                )
            }

            if (area.hasMine) {
                val color = theme.palette.uncovered
                drawAsset(
                    batch = batch,
                    texture = it.mine,
                    color = color.toOppositeMax(1.0f),
                    scale = BASE_ICON_SCALE
                )
            }
        }
    }

    private fun drawPressed(batch: Batch, isOdd: Boolean) {
        if ((isPressed || focusScale > 1.0f)) {
            if (area.isCovered) {
                val coverColor = if (isOdd) { theme.palette.coveredOdd } else { theme.palette.covered }

                GdxLocal.gameTextures?.detailedArea?.let {
                    batch.drawTexture(
                        texture = it,
                        x = x,
                        y = y,
                        width = width,
                        height = height,
                        color = coverColor.toGdxColor(1.0f),
                        blend = true,
                    )

                    batch.drawTexture(
                        texture = it,
                        x = x - width * (focusScale - 1.0f) * 0.5f,
                        y = y - height * (focusScale - 1.0f) * 0.5f,
                        width = width * focusScale,
                        height = height * focusScale,
                        color = coverColor.toGdxColor(1.0f).dim(0.8f - (focusScale - 1.0f)),
                        blend = true,
                    )
                }
            } else {
                GdxLocal.gameTextures?.detailedArea?.let {
                    val color = theme.palette.background
                    batch.drawTexture(
                        texture = it,
                        x = x - width * (focusScale - 1.0f) * 0.5f,
                        y = y - height * (focusScale - 1.0f) * 0.5f,
                        width = width * focusScale,
                        height = height * focusScale,
                        color = color.toOppositeMax(0.25f).dim(0.8f - (focusScale - 1.0f)),
                        blend = true,
                    )
                }
            }
        }
    }

    private fun drawFocusMarker(batch: Batch) {
        GdxLocal.gameTextures?.areaHighlight?.let {
            val color = theme.palette.highlight
            batch.drawTexture(
                texture = it,
                x = x - width * (focusScale - 1.0f) * 0.5f,
                y = y - height * (focusScale - 1.0f) * 0.5f,
                width = width * focusScale,
                height = height * focusScale,
                color = color.toGdxColor().dim(0.8f - (focusScale - 1.0f)),
                blend = true,
            )
        }
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        val isOdd: Boolean = if (area.posY % 2 == 0) { area.posX % 2 != 0 } else { area.posX % 2 == 0 }

        batch?.run {
            drawBackground(this, isOdd)

            if (area.isCovered) {
                drawCovered(this, isOdd)
                drawPressed(this, isOdd)
                drawCoveredIcons(this)
            } else {
                if (area.hasMine) {
                    drawMineBackground(this)
                }
                drawPressed(this, isOdd)
                drawUncoveredIcons(this)
            }

            if (GdxLocal.currentFocus?.id == area.id) {
                drawFocusMarker(this)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AreaActor

        if (area != other.area) return false
        if (pieces != other.pieces) return false
        if (areaForm != other.areaForm) return false

        return true
    }

    override fun hashCode(): Int {
        var result = area.hashCode()
        result = 31 * result + pieces.hashCode()
        result = 31 * result + (areaForm?.hashCode() ?: 0)
        return result
    }

    companion object {
        const val MIN_SCALE = 1.0f
        const val MAX_SCALE = 1.15f
        const val BASE_ICON_SCALE = 0.8f

        private fun Area.canLigatureTo(area: Area): Boolean {
            return isCovered && mark.ligatureId == area.mark.ligatureId
        }
    }
}
