package dev.lucasnlm.antimine.gdx.actors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import dev.lucasnlm.antimine.core.AreaExt.getNeighborIdAtPos
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.gdx.AtlasNames
import dev.lucasnlm.antimine.gdx.GameContext
import dev.lucasnlm.antimine.gdx.GdxExt.alpha
import dev.lucasnlm.antimine.gdx.GdxExt.dim
import dev.lucasnlm.antimine.gdx.GdxExt.drawAsset
import dev.lucasnlm.antimine.gdx.GdxExt.drawRegion
import dev.lucasnlm.antimine.gdx.GdxExt.toGdxColor
import dev.lucasnlm.antimine.gdx.GdxExt.toInverseBackOrWhite
import dev.lucasnlm.antimine.gdx.actors.AreaForm.AREA_FULL_FORM
import dev.lucasnlm.antimine.gdx.actors.AreaForm.AREA_NO_FORM
import dev.lucasnlm.antimine.gdx.actors.AreaForm.areaFormOf
import dev.lucasnlm.antimine.gdx.actors.AreaForm.toAtlasNames
import dev.lucasnlm.antimine.gdx.models.GameRenderingContext
import dev.lucasnlm.antimine.ui.model.minesAround
import dev.lucasnlm.antimine.ui.repository.Themes

/**
 * Wraps an [Area] and draws it on the screen.
 */
class AreaActor(
    inputListener: InputListener,
    var isPressed: Boolean = false,
    private var focusScale: Float = 1.0f,
    private var pieces: Set<String> = setOf(),
    private val gameRenderingContext: GameRenderingContext,
) : Actor() {
    var area: Area? = null

    private var areaForm: Int? = null

    private var topId: Int = NO_LINK
    private var bottomId: Int = NO_LINK
    private var leftId: Int = NO_LINK
    private var rightId: Int = NO_LINK
    private var topLeftId: Int = NO_LINK
    private var topRightId: Int = NO_LINK
    private var bottomLeftId: Int = NO_LINK
    private var bottomRightId: Int = NO_LINK

    init {
        width = gameRenderingContext.areaSize
        height = gameRenderingContext.areaSize

        addListener(inputListener)
    }

    fun bindArea(
        reset: Boolean,
        area: Area,
        field: List<Area>,
        checkShape: Boolean,
    ) {
        if (reset) {
            this.isPressed = false
        }

        if (this.area != area) {
            x = area.posX * width
            y = area.posY * height
            refreshLinks(area, field)

            this.area = area
        }

        if (checkShape) {
            val newForm =
                when {
                    area.isCovered && gameRenderingContext.joinAreas -> {
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
                pieces = newForm.toAtlasNames()
            }
        }
    }

    override fun act(delta: Float) {
        super.act(delta)
        val area = this.area ?: return

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
        val area = this.area ?: return
        val backgroundOnAll = gameRenderingContext.appSkin.backgroundOnAll

        if ((backgroundOnAll || !isOdd) && !area.isCovered && GameContext.zoomLevelAlpha > 0.0f) {
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
        val area = this.area ?: return

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
                    batch.drawRegion(
                        texture = GameContext.gameTextures!!.pieces[piece]!!,
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

    private fun drawMineBackground(batch: Batch) {
        val coverColor = Color(0.8f, 0.3f, 0.3f, 1.0f)

        GameContext.atlas?.let { atlas ->
            pieces.forEach { piece ->
                batch.drawRegion(
                    texture = atlas.findRegion(piece),
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

    private fun drawCoveredIcons(batch: Batch) {
        val area = this.area ?: return
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
        val area = this.area ?: return
        GameContext.gameTextures?.let {
            if (area.minesAround > 0) {
                drawAsset(
                    batch = batch,
                    texture = it.aroundMines[area.minesAround - 1],
                    color =
                        if (area.dimNumber) {
                            gameRenderingContext.theme.palette
                                .minesAround(area.minesAround - 1)
                                .toGdxColor(GameContext.zoomLevelAlpha * 0.45f)
                                .dim(0.5f)
                        } else {
                            gameRenderingContext.theme.palette
                                .minesAround(area.minesAround - 1)
                                .toGdxColor(GameContext.zoomLevelAlpha)
                        },
                )
            } else if (area.hasMine) {
                val color =
                    if (gameRenderingContext.appSkin.canTint) {
                        gameRenderingContext.theme.palette.covered.toInverseBackOrWhite(1.0f)
                    } else {
                        Color.WHITE
                    }
                drawAsset(
                    batch = batch,
                    texture = it.mine,
                    color = color,
                    scale = BASE_ICON_SCALE,
                )
            }
        }
    }

    private fun drawPressed(
        batch: Batch,
        isOdd: Boolean,
    ) {
        val area = this.area ?: return
        if ((isPressed || focusScale > 1.0f)) {
            if (area.isCovered) {
                val tint = GameContext.canTintAreas
                val coverColor =
                    when {
                        tint ->
                            if (isOdd) {
                                gameRenderingContext.theme.palette.coveredOdd
                            } else {
                                gameRenderingContext.theme.palette.covered
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
                    val color = gameRenderingContext.theme.palette.background
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
        val area = this.area ?: return

        batch?.run {
            val isOdd = area.isOdd

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

    private fun refreshLinks(
        area: Area,
        field: List<Area>,
    ) {
        if (gameRenderingContext.joinAreas) {
            topId = area.getNeighborIdAtPos(field, 0, 1)
            bottomId = area.getNeighborIdAtPos(field, 0, -1)
            leftId = area.getNeighborIdAtPos(field, -1, 0)
            rightId = area.getNeighborIdAtPos(field, 1, 0)
            topLeftId = area.getNeighborIdAtPos(field, -1, 1)
            topRightId = area.getNeighborIdAtPos(field, 1, 1)
            bottomLeftId = area.getNeighborIdAtPos(field, -1, -1)
            bottomRightId = area.getNeighborIdAtPos(field, 1, -1)
        } else {
            topId = NO_LINK
            bottomId = NO_LINK
            leftId = NO_LINK
            rightId = NO_LINK
            topLeftId = NO_LINK
            topRightId = NO_LINK
            bottomLeftId = NO_LINK
            bottomRightId = NO_LINK
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
        const val NO_LINK = -1

        private fun Area.canLinkTo(area: Area): Boolean {
            return isCovered && mark.ligatureMask == area.mark.ligatureMask
        }
    }
}
