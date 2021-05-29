package dev.lucasnlm.antimine.gdx

import android.content.Context
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.input.GestureDetector
import dev.lucasnlm.antimine.core.isAndroidTv
import dev.lucasnlm.antimine.core.isAndroidWearOs
import dev.lucasnlm.antimine.core.isPortrait
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.gdx.controller.GameInputController
import dev.lucasnlm.antimine.gdx.models.ActionSettings
import dev.lucasnlm.antimine.gdx.models.GameTextures
import dev.lucasnlm.antimine.gdx.models.InternalPadding
import dev.lucasnlm.antimine.gdx.models.RenderQuality
import dev.lucasnlm.antimine.gdx.models.RenderSettings
import dev.lucasnlm.antimine.gdx.shaders.BlurShader
import dev.lucasnlm.antimine.gdx.stages.MinefieldStage
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.ui.ext.blue
import dev.lucasnlm.antimine.ui.ext.green
import dev.lucasnlm.antimine.ui.ext.red
import dev.lucasnlm.antimine.ui.model.AppTheme

class GameApplicationListener(
    private val context: Context,
    private val preferencesRepository: IPreferencesRepository,
    private val dimensionRepository: IDimensionRepository,
    private val quality: RenderQuality,
    private val theme: AppTheme,
    private val onSingleTap: (Int) -> Unit,
    private val onDoubleTap: (Int) -> Unit,
    private val onLongTap: (Int) -> Unit,
    private val crashLogger: (String) -> Unit,
) : ApplicationAdapter() {

    private val assetManager = AssetManager()

    private var minefieldStage: MinefieldStage? = null
    private var boundAreas: List<Area> = listOf()
    private var boundMinefield: Minefield? = null
    private val useBlur =
        !context.isAndroidTv() && !context.isAndroidWearOs() && quality != RenderQuality.Low && context.isPortrait()

    private var batch: SpriteBatch? = null
    private var mainFrameBuffer: FrameBuffer? = null
    private var blurFrameBuffer: FrameBuffer? = null
    private var blurShader: ShaderProgram? = null

    private val renderSettings = RenderSettings(
        theme = theme,
        internalPadding = getInternalPadding(),
        areaSize = dimensionRepository.areaSize(),
        navigationBarHeight = dimensionRepository.navigationBarHeight().toFloat(),
        appBarWithStatusHeight = dimensionRepository.actionBarSizeWithStatus().toFloat(),
        appBarHeight = if (context.isPortrait()) { dimensionRepository.actionBarSize().toFloat() } else { 0f },
        radius = preferencesRepository.squareRadius().toFloat(),
        squareDivider = preferencesRepository.squareDivider().toFloat() * 2,
        joinAreas = preferencesRepository.squareDivider() == 0,
        quality = quality,
    )

    private var actionSettings = with(preferencesRepository) {
        val control = controlStyle()
        ActionSettings(
            handleDoubleTaps = control == ControlStyle.DoubleClick || control == ControlStyle.DoubleClickInverted,
            longTapTimeout = preferencesRepository.customLongPressTimeout(),
            doubleTapTimeout = preferencesRepository.getDoubleClickTimeout(),
            touchSensibility = preferencesRepository.touchSensibility() * preferencesRepository.touchSensibility(),
        )
    }

    private val minefieldInputController = GameInputController(
        onChangeZoom = {
            GdxLocal.zoom = it
            minefieldStage?.scaleZoom(it)
        }
    )

    override fun create() {
        super.create()

        val width = Gdx.graphics.width
        val height = Gdx.graphics.height

        if (useBlur) {
            batch = SpriteBatch()
            mainFrameBuffer = FrameBuffer(Pixmap.Format.RGB888, width, height, false)
            blurFrameBuffer = FrameBuffer(Pixmap.Format.RGBA8888, width, height, false)

            blurShader = ShaderProgram(BlurShader.vert(), BlurShader.frag()).apply {
                bind()
                if (log.isNotBlank()) {
                    crashLogger("Fail to compile shader. Error: $log")
                }

                setUniformf(BlurShader.resolution, width.toFloat())
            }
        }

        assetManager.load(TextureConstants.atlasName, TextureAtlas::class.java)
        assetManager.finishLoading()

        minefieldStage = MinefieldStage(
            renderSettings = renderSettings,
            actionSettings = actionSettings,
            onSingleTap = onSingleTap,
            onDoubleTap = onDoubleTap,
            onLongTouch = onLongTap,
            forceFocus = context.isAndroidTv(),
        ).apply {
            bindField(boundAreas)
            bindSize(boundMinefield)
        }

        GdxLocal.run {
            val expectedSize = dimensionRepository.areaSize()
            val radiusLevel = preferencesRepository.squareRadius()
            val atlas = assetManager.get<TextureAtlas>(TextureConstants.atlasName)

            animationScale = if (preferencesRepository.useAnimations()) 1f else 100.0f

            areaAtlas = AreaAssetBuilder.getAreaTextureAtlas(
                radiusLevel = radiusLevel,
                squareDivider = renderSettings.squareDivider,
                quality = quality,
            )
            textureAtlas = atlas
            gameTextures = GameTextures(
                areaHighlight = AreaAssetBuilder.getAreaBorderTexture(
                    expectedSize = expectedSize,
                    radiusLevel = radiusLevel,
                ),
                areaCovered = AreaAssetBuilder.getAreaTexture(
                    expectedSize = expectedSize,
                    radiusLevel = radiusLevel,
                ),
                aroundMines = listOf(
                    atlas.findRegion(TextureConstants.around1),
                    atlas.findRegion(TextureConstants.around2),
                    atlas.findRegion(TextureConstants.around3),
                    atlas.findRegion(TextureConstants.around4),
                    atlas.findRegion(TextureConstants.around5),
                    atlas.findRegion(TextureConstants.around6),
                    atlas.findRegion(TextureConstants.around7),
                    atlas.findRegion(TextureConstants.around8),
                ),
                mine = atlas.findRegion(TextureConstants.mine),
                flag = atlas.findRegion(TextureConstants.flag),
                question = atlas.findRegion(TextureConstants.question),
                detailedArea = AreaAssetBuilder.getAreaTexture(
                    expectedSize = expectedSize,
                    radiusLevel = radiusLevel,
                ),
            )
        }

        Gdx.input.inputProcessor = InputMultiplexer(GestureDetector(minefieldInputController), minefieldStage)
        Gdx.graphics.isContinuousRendering = false
    }

    override fun dispose() {
        super.dispose()
        blurShader?.dispose()
        mainFrameBuffer?.dispose()
        blurFrameBuffer?.dispose()
        batch?.dispose()

        GdxLocal.run {
            zoomLevelAlpha = 1.0f
            animationScale = 1.0f
            gameTextures?.run {
                areaHighlight.dispose()
                detailedArea.dispose()
            }
            textureAtlas?.dispose()
            areaAtlas?.dispose()
            areaAtlas = null
            textureAtlas = null
        }

        Gdx.input.inputProcessor = null
        boundMinefield = null
        assetManager.dispose()
    }

    override fun render() {
        super.render()
        val mainFrameBuffer = this.mainFrameBuffer
        val minefieldStage = this.minefieldStage
        val batch = this.batch
        val blurShader = this.blurShader

        if (useBlur) {
            val width = Gdx.graphics.width
            val height = Gdx.graphics.height

            mainFrameBuffer?.begin()
            minefieldStage?.run {
                theme.palette.background.run {
                    Gdx.gl.glClearColor(red(), green(), blue(), 1f)
                    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
                }

                act()
                draw()
            }
            mainFrameBuffer?.end()

            batch?.run {
                begin()

                theme.palette.background.run {
                    Gdx.gl.glClearColor(red(), 0f, blue(), 1f)
                    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
                }

                shader = blurShader?.apply {
                    setUniformf(BlurShader.direction, 1.0f, 1.0f)
                    setUniformf(BlurShader.radius, 2.0f)

                    if (context.isPortrait()) {
                        setUniformf(BlurShader.blurTop, (1.0f - (renderSettings.appBarWithStatusHeight / height)))
                        setUniformf(BlurShader.blurBottom, (renderSettings.navigationBarHeight / height))
                        setUniformf(BlurShader.blurStart, 0.0f)
                        setUniformf(BlurShader.blurEnd, 1.0f)
                    } else {
                        setUniformf(BlurShader.blurTop, 1.0f)
                        setUniformf(BlurShader.blurBottom, 0.0f)
                        setUniformf(BlurShader.blurStart, (renderSettings.appBarHeight / width))
                        setUniformf(BlurShader.blurEnd, 1.0f - (renderSettings.navigationBarHeight / width))
                    }
                }

                mainFrameBuffer?.let {
                    draw(
                        it.colorBufferTexture,
                        0.0f,
                        0.0f,
                        0.0f,
                        0.0f,
                        width.toFloat(),
                        height.toFloat(),
                        1.0f,
                        1.0f,
                        0.0f,
                        0,
                        0,
                        width,
                        height,
                        false,
                        true,
                    )
                }

                end()
            }
        } else {
            minefieldStage?.run {
                theme.palette.background.run {
                    Gdx.gl.glClearColor(red(), green(), blue(), 1f)
                    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
                }

                act()
                draw()
            }
        }
    }

    private fun getInternalPadding(): InternalPadding {
        val padding = dimensionRepository.areaSize()
        return when {
            context.isAndroidTv() -> {
                InternalPadding(
                    start = padding,
                    end = padding,
                    bottom = padding,
                    top = padding,
                )
            }
            context.isPortrait() -> {
                InternalPadding(
                    start = padding,
                    end = padding,
                    bottom = padding,
                    top = padding,
                )
            }
            else -> {
                InternalPadding(
                    start = padding,
                    end = padding,
                    bottom = padding,
                    top = padding,
                )
            }
        }
    }

    fun bindMinefield(minefield: Minefield) {
        boundMinefield = minefield
        minefieldStage?.bindSize(minefield)
        Gdx.graphics.requestRendering()
    }

    fun bindField(field: List<Area>) {
        boundAreas = field
        minefieldStage?.bindField(field)
        Gdx.graphics.requestRendering()
        GdxLocal.hasHighlightAreas = field.firstOrNull { it.highlighted } != null
    }

    fun setActionsEnabled(enabled: Boolean) {
        GdxLocal.actionsEnabled = enabled
    }

    fun recenter() {
        minefieldStage?.centerCamera()
    }

    fun refreshZoom() {
        minefieldStage?.setZoom(GdxLocal.zoom)
    }

    fun refreshSettings() {
        actionSettings = with(preferencesRepository) {
            val control = controlStyle()
            ActionSettings(
                handleDoubleTaps = control == ControlStyle.DoubleClick || control == ControlStyle.DoubleClickInverted,
                longTapTimeout = preferencesRepository.customLongPressTimeout(),
                doubleTapTimeout = preferencesRepository.getDoubleClickTimeout(),
                touchSensibility = preferencesRepository.touchSensibility() * preferencesRepository.touchSensibility(),
            )
        }

        minefieldStage?.updateActionSettings(actionSettings)
    }
}
