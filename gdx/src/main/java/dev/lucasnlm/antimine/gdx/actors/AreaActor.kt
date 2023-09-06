package dev.lucasnlm.antimine.gdx.actors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import dev.lucasnlm.antimine.core.getNeighborIdAtPos
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.gdx.AtlasNames
import dev.lucasnlm.antimine.gdx.GameContext
import dev.lucasnlm.antimine.gdx.alpha
import dev.lucasnlm.antimine.gdx.dim
import dev.lucasnlm.antimine.gdx.drawAsset
import dev.lucasnlm.antimine.gdx.drawRegion
import dev.lucasnlm.antimine.gdx.events.GdxEvent
import dev.lucasnlm.antimine.gdx.toGdxColor
import dev.lucasnlm.antimine.gdx.toInverseBackOrWhite
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.model.minesAround
import dev.lucasnlm.antimine.ui.repository.Themes

class AreaActor(
    size: Float,
    field: List<Area>,
    enableLigatures: Boolean,
    private var area: Area,
    private var focusScale: Float = 1.0f,
    private var isPressed: Boolean = false,
    private val pieces: MutableMap<String, Boolean> = mutableMapOf(),
    private val theme: AppTheme,
    private val onInputEvent: (GdxEvent) -> Unit,
) : Actor() {

    private var areaForm: Int? = null

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

        addListener(
            object : InputListener() {
                override fun touchUp(
                    event: InputEvent?,
                    x: Float,
                    y: Float,
                    pointer: Int,
                    button: Int,
                ) {
                    super.touchUp(event, x, y, pointer, button)
                    onInputEvent(GdxEvent.TouchUpEvent(area.id))
                    isPressed = false
                    toBack()
                    Gdx.graphics.requestRendering()
                }

                override fun touchDown(
                    event: InputEvent,
                    x: Float,
                    y: Float,
                    pointer: Int,
                    button: Int,
                ): Boolean {
                    toFront()
                    isPressed = true
                    onInputEvent(GdxEvent.TouchDownEvent(area.id))
                    Gdx.graphics.requestRendering()
                    return true
                }
            },
        )

        topId = area.getNeighborIdAtPos(field, 0, 1)
        bottomId = area.getNeighborIdAtPos(field, 0, -1)
        leftId = area.getNeighborIdAtPos(field, -1, 0)
        rightId = area.getNeighborIdAtPos(field, 1, 0)
        topLeftId = area.getNeighborIdAtPos(field, -1, 1)
        topRightId = area.getNeighborIdAtPos(field, 1, 1)
        bottomLeftId = area.getNeighborIdAtPos(field, -1, -1)
        bottomRightId = area.getNeighborIdAtPos(field, 1, -1)

        bindArea(reset = true, ligatureEnabled = enableLigatures, area = area, field = field)
    }

    fun boundAreaId() = area.id

    fun bindArea(
        reset: Boolean,
        ligatureEnabled: Boolean,
        area: Area,
        field: List<Area>,
    ) {
        if (reset) {
            this.isPressed = false
        }

        val newForm =
            when {
                area.isCovered && ligatureEnabled -> {
                    areaFormOf(
                        top = field.getOrNull(topId)?.canLinkTo(area) == true,
                        bottom = field.getOrNull(bottomId)?.canLinkTo(area) == true,
                        left = field.getOrNull(leftId)?.canLinkTo(area) == true,
                        right = field.getOrNull(rightId)?.canLinkTo(area) == true,
                        topLeft = field.getOrNull(topLeftId)?.canLinkTo(area) == true,
                        topRight = field.getOrNull(topRightId)?.canLinkTo(area) == true,
                        bottomLeft = field.getOrNull(bottomLeftId)?.canLinkTo(area) == true,
                        bottomRight = field.getOrNull(bottomRightId)?.canLinkTo(area) == true,
                    )
                }
                else -> {
                    AREA_NO_FORM
                }
            }

        if ((area.isCovered || area.hasMine) && this.areaForm != newForm) {
            this.areaForm = newForm
            pieces.putAll(newForm.toAtlasNames())
        }

        this.area = area
    }

    override fun act(delta: Float) {
        super.act(delta)
        val area = this.area

        touchable =
            if ((area.isCovered || area.minesAround > 0) && GameContext.actionsEnabled) {
                Touchable.enabled
            } else {
                Touchable.disabled
            }

        val newFocusScale =
            if (isPressed) {
                (focusScale + Gdx.graphics.deltaTime).coerceAtMost(MAX_SCALE)
            } else {
                (focusScale - Gdx.graphics.deltaTime).coerceAtLeast(MIN_SCALE)
            }

        if (newFocusScale != focusScale) {
            focusScale = newFocusScale
            Gdx.graphics.requestRendering()
        }
    }

    private fun drawBackground(
        batch: Batch,
        isOdd: Boolean,
    ) {
        if (!isOdd && !area.isCovered && GameContext.zoomLevelAlpha > 0.0f) {
            GameContext.gameTextures?.areaBackground?.let {
                batch.drawRegion(
                    texture = it,
                    x = x,
                    y = y,
                    width = width,
                    height = height,
                    blend = false,
                    color = GameContext.backgroundColor,
                )
            }
        }
    }

    private fun drawCovered(batch: Batch) {
        val coverColor =
            when {
                !GameContext.canTintAreas -> GameContext.whiteColor
                area.mark.isNotNone() -> GameContext.coveredMarkedAreaColor
                else -> GameContext.coveredAreaColor
            }

        GameContext.atlas?.let { atlas ->
            if (areaForm == AREA_FULL_FORM) {
                batch.drawRegion(
                    texture = atlas.findRegion(AtlasNames.FULL),
                    x = x - 0.5f,
                    y = y - 0.5f,
                    width = width + 0.5f,
                    height = height + 0.5f,
                    color = coverColor,
                    blend = false,
                )
            } else {
                pieces.forEach { piece ->
                    if (piece.value) {
                        batch.drawRegion(
                            texture = GameContext.gameTextures!!.pieces[piece.key]!!,
                            x = x - 0.5f,
                            y = y - 0.5f,
                            width = width + 0.5f,
                            height = height + 0.5f,
                            color = coverColor,
                            blend = false,
                        )
                    }
                }
            }
        }
    }

    private fun drawMineBackground(batch: Batch) {
        val coverColor = Color(0.8f, 0.3f, 0.3f, 1.0f)

        GameContext.atlas?.let { atlas ->
            pieces.forEach { piece ->
                if (piece.value) {
                    batch.drawRegion(
                        texture = atlas.findRegion(piece.key),
                        x = x - 0.5f,
                        y = y - 0.5f,
                        width = width + 1.0f,
                        height = height + 1.0f,
                        color = coverColor,
                        blend = false,
                    )
                }
            }
        }
    }

    private fun drawCoveredIcons(batch: Batch) {
        val isAboveOthers = isPressed

        GameContext.gameTextures?.let {
            when {
                area.mark.isFlag() -> {
                    val color =
                        if (area.mistake) {
                            Color(0.8f, 0.3f, 0.3f, 1.0f)
                        } else {
                            GameContext.markColor
                        }

                    drawAsset(
                        batch = batch,
                        texture = it.flag,
                        color = color,
                        scale = if (isAboveOthers) focusScale else BASE_ICON_SCALE,
                    )
                }
                area.mark.isQuestion() -> {
                    val color = GameContext.markColor

                    drawAsset(
                        batch = batch,
                        texture = it.question,
                        color = color,
                        scale = if (isAboveOthers) focusScale else BASE_ICON_SCALE,
                    )
                }
                area.revealed -> {
                    val color = GameContext.markColor.cpy()

                    drawAsset(
                        batch = batch,
                        texture = it.mine,
                        color = color.alpha(0.65f),
                        scale = BASE_ICON_SCALE,
                    )
                }
            }
        }
    }

    private fun drawUncoveredIcons(batch: Batch) {
        GameContext.gameTextures?.let {
            if (area.minesAround > 0) {
                drawAsset(
                    batch = batch,
                    texture = it.aroundMines[area.minesAround - 1],
                    color =
                        if (area.dimNumber) {
                            theme.palette
                                .minesAround(area.minesAround - 1)
                                .toGdxColor(GameContext.zoomLevelAlpha * 0.45f)
                                .dim(0.5f)
                        } else {
                            theme.palette
                                .minesAround(area.minesAround - 1)
                                .toGdxColor(GameContext.zoomLevelAlpha)
                        },
                )
            } else if (area.hasMine) {
                val color = theme.palette.uncovered
                drawAsset(
                    batch = batch,
                    texture = it.mine,
                    color = color.toInverseBackOrWhite(1.0f),
                    scale = BASE_ICON_SCALE,
                )
            }
        }
    }

    private fun drawPressed(
        batch: Batch,
        isOdd: Boolean,
    ) {
        if ((isPressed || focusScale > 1.0f)) {
            if (area.isCovered) {
                val tint = GameContext.canTintAreas
                val coverColor =
                    when {
                        tint ->
                            if (isOdd) {
                                theme.palette.coveredOdd
                            } else {
                                theme.palette.covered
                            }
                        else -> Themes.WHITE
                    }.toGdxColor(0.5f)

                GameContext.gameTextures?.detailedArea?.let {
                    batch.drawRegion(
                        texture = it,
                        x = x,
                        y = y,
                        width = width,
                        height = height,
                        color = coverColor,
                        blend = true,
                    )

                    batch.drawRegion(
                        texture = it,
                        x = x - width * (focusScale - 1.0f) * 0.5f,
                        y = y - height * (focusScale - 1.0f) * 0.5f,
                        width = width * focusScale,
                        height = height * focusScale,
                        color = coverColor.dim(0.8f - (focusScale - 1.0f)),
                        blend = true,
                    )
                }
            } else {
                GameContext.gameTextures?.detailedArea?.let {
                    val color = theme.palette.background
                    batch.drawRegion(
                        texture = it,
                        x = x - width * (focusScale - 1.0f) * 0.5f,
                        y = y - height * (focusScale - 1.0f) * 0.5f,
                        width = width * focusScale,
                        height = height * focusScale,
                        color = color.toInverseBackOrWhite(0.25f).dim(0.8f - (focusScale - 1.0f)),
                        blend = true,
                    )
                }
            }
        }
    }

    override fun draw(
        batch: Batch?,
        parentAlpha: Float,
    ) {
        super.draw(batch, parentAlpha)

        batch?.run {
            val isOdd: Boolean =
                if (area.posY % 2 == 0) {
                    area.posX % 2 != 0
                } else {
                    area.posX % 2 == 0
                }

            drawBackground(this, isOdd)

            if (area.isCovered) {
                drawCovered(this)
                drawPressed(this, isOdd)
                drawCoveredIcons(this)
            } else {
                if (area.hasMine) {
                    drawMineBackground(this)
                }
                drawPressed(this, isOdd)
                drawUncoveredIcons(this)
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

        private fun Area.canLinkTo(area: Area): Boolean {
            return isCovered && mark.ligatureMask == area.mark.ligatureMask
        }
    }
}
