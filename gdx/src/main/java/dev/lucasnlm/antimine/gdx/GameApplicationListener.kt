package dev.lucasnlm.antimine.gdx

import android.content.Context
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.input.GestureDetector
import dev.lucasnlm.antimine.core.IAppVersionManager
import dev.lucasnlm.antimine.core.isPortrait
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.gdx.controller.GameInputController
import dev.lucasnlm.antimine.gdx.models.ActionSettings
import dev.lucasnlm.antimine.gdx.models.GameTextures
import dev.lucasnlm.antimine.gdx.models.InternalPadding
import dev.lucasnlm.antimine.gdx.models.RenderSettings
import dev.lucasnlm.antimine.gdx.shaders.BlurShader
import dev.lucasnlm.antimine.gdx.stages.MinefieldStage
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.ui.ext.blue
import dev.lucasnlm.antimine.ui.ext.green
import dev.lucasnlm.antimine.ui.ext.red
import dev.lucasnlm.antimine.ui.repository.IThemeRepository

class GameApplicationListener(
    private val context: Context,
    private val appVersion: IAppVersionManager,
    private val preferencesRepository: IPreferencesRepository,
    private val themeRepository: IThemeRepository,
    private val dimensionRepository: IDimensionRepository,
    private val onSingleTap: (Int) -> Unit,
    private val onDoubleTap: (Int) -> Unit,
    private val onLongTap: (Int) -> Unit,
    private val onEngineReady: () -> Unit,
    private val crashLogger: (String) -> Unit,
) : ApplicationAdapter() {
    private var minefieldStage: MinefieldStage? = null
    private var boundAreas: List<Area> = listOf()
    private var boundMinefield: Minefield? = null
    private val useBlur = context.isPortrait() && !appVersion.isWatch()

    private var batch: SpriteBatch? = null
    private var mainFrameBuffer: FrameBuffer? = null
    private var blurFrameBuffer: FrameBuffer? = null
    private var blurShader: ShaderProgram? = null

    private val renderSettings = RenderSettings(
        theme = themeRepository.getTheme(),
        internalPadding = getInternalPadding(),
        areaSize = dimensionRepository.areaSize(),
        navigationBarHeight = dimensionRepository.navigationBarHeight().toFloat(),
        appBarWithStatusHeight = dimensionRepository.actionBarSizeWithStatus().toFloat(),
        appBarHeight = if (context.isPortrait()) { dimensionRepository.actionBarSize().toFloat() } else { 0f },
        joinAreas = themeRepository.getSkin().joinAreas,
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
            GameContext.zoom = it
            minefieldStage?.scaleZoom(it)
        },
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

        minefieldStage = MinefieldStage(
            screenWidth = width.toFloat(),
            screenHeight = height.toFloat(),
            renderSettings = renderSettings,
            actionSettings = actionSettings,
            onSingleTap = onSingleTap,
            onDoubleTap = onDoubleTap,
            onLongTouch = onLongTap,
            onEngineReady = onEngineReady,
        ).apply {
            bindField(boundAreas)
            bindSize(boundMinefield)
        }

        val currentSkin = themeRepository.getSkin()

        GameContext.run {
            canTintAreas = currentSkin.canTint

            atlas = GameTextureAtlas.loadTextureAtlas(
                skinFile = currentSkin.file,
                defaultBackground = currentSkin.background,
            ).apply {
                gameTextures = GameTextures(
                    areaBackground = findRegion(AtlasNames.singleBackground),
                    aroundMines = listOf(
                        AtlasNames.number1,
                        AtlasNames.number2,
                        AtlasNames.number3,
                        AtlasNames.number4,
                        AtlasNames.number5,
                        AtlasNames.number6,
                        AtlasNames.number7,
                        AtlasNames.number8,
                    ).map(::findRegion),
                    pieces = listOf(
                        AtlasNames.core,
                        AtlasNames.bottom,
                        AtlasNames.top,
                        AtlasNames.right,
                        AtlasNames.left,
                        AtlasNames.cornerTopLeft,
                        AtlasNames.cornerTopRight,
                        AtlasNames.cornerBottomRight,
                        AtlasNames.cornerBottomLeft,
                        AtlasNames.borderCornerTopRight,
                        AtlasNames.borderCornerTopLeft,
                        AtlasNames.borderCornerBottomRight,
                        AtlasNames.borderCornerBottomLeft,
                        AtlasNames.fillTopLeft,
                        AtlasNames.fillTopRight,
                        AtlasNames.fillBottomRight,
                        AtlasNames.fillBottomLeft,
                        AtlasNames.full,
                    ).associateWith(::findRegion),
                    mine = findRegion(AtlasNames.mine),
                    flag = findRegion(AtlasNames.flag),
                    question = findRegion(AtlasNames.question),
                    detailedArea = findRegion(AtlasNames.single),
                )
            }
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

        GameContext.run {
            zoomLevelAlpha = 1.0f
            gameTextures = null
            atlas?.dispose()
            atlas = null
        }

        Gdx.input.inputProcessor = null
        boundMinefield = null
    }

    fun onPause() {
        GameContext.run {
            zoom = 1.0f
            minefieldStage?.setZoom(1.0f)
        }
    }

    override fun render() {
        super.render()
        val mainFrameBuffer = this.mainFrameBuffer
        val minefieldStage = this.minefieldStage
        val batch = this.batch
        val blurShader = this.blurShader
        val currentTheme = themeRepository.getTheme()

        if (useBlur) {
            val width = Gdx.graphics.width
            val height = Gdx.graphics.height

            mainFrameBuffer?.begin()
            minefieldStage?.run {
                currentTheme.palette.background.run {
                    Gdx.gl.glClearColor(red(), green(), blue(), 1f)
                    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
                }

                act()
                draw()
            }
            mainFrameBuffer?.end()

            if (!appVersion.isValid()) {
                Thread.sleep(500L)
            }

            batch?.run {
                begin()

                currentTheme.palette.background.run {
                    Gdx.gl.glClearColor(red(), green(), blue(), 1f)
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
            if (!appVersion.isValid()) {
                Thread.sleep(500L)
            }

            minefieldStage?.run {
                currentTheme.palette.background.run {
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
    }

    fun setActionsEnabled(enabled: Boolean) {
        GameContext.actionsEnabled = enabled
    }

    fun onChangeGame() {
        minefieldStage?.onChangeGame()
    }

    fun refreshZoom() {
        minefieldStage?.setZoom(GameContext.zoom)
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
