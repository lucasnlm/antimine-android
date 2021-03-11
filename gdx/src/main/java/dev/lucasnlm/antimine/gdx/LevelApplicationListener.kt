package dev.lucasnlm.antimine.gdx

import android.content.Context
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import dev.lucasnlm.antimine.core.isAndroidTv
import dev.lucasnlm.antimine.core.isPortrait
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.gdx.actors.AreaActor
import dev.lucasnlm.antimine.gdx.models.InternalPadding
import dev.lucasnlm.antimine.gdx.models.RenderSettings
import dev.lucasnlm.antimine.gdx.screens.MinefieldScreen
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.ui.ext.blue
import dev.lucasnlm.antimine.ui.ext.green
import dev.lucasnlm.antimine.ui.ext.red
import dev.lucasnlm.antimine.ui.model.AppTheme

class LevelApplicationListener(
    private val context: Context,
    private val dimensionRepository: IDimensionRepository,
    private val theme: AppTheme,
) : ApplicationAdapter() {

    private var minefieldScreen: MinefieldScreen? = null
    private val areaSize = dimensionRepository.areaSize()
    private var boundAreas: Iterable<AreaActor>? = null
    private var boundMinefield: Minefield? = null

    override fun create() {
        super.create()
        //Gdx.graphics.isContinuousRendering = false
        minefieldScreen = MinefieldScreen(
            renderSettings = RenderSettings(
                internalPadding = getInternalPadding(),
                areaSize = dimensionRepository.areaSize(),
            )
        ).apply {
            boundAreas?.forEach(::addActor)
            boundMinefield?.let(::bindMinefield)
            Gdx.input.inputProcessor = this
        }
    }

    override fun dispose() {
        super.dispose()
        Gdx.input.inputProcessor = null
        boundAreas = null
        boundMinefield = null
    }

    override fun render() {
        super.render()
        theme.palette.background.run {
            Gdx.gl.glClearColor(red(), green(), blue(), 1f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        }


        minefieldScreen?.run {
            camera.update()
            //camera.position.set(10f, camera.viewportWidth * 0.05f, 0.0f)
//            camera.position.set(10f, 10f, 0f)
//            camera.update()
            draw()
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
                    top = 0f,
                )
            }
            else -> {
                InternalPadding(
                    start = 0f,
                    end = padding,
                    bottom = padding,
                    top = padding,
                )
            }
        }
    }

    fun bindMinefield(minefield: Minefield) {
        boundMinefield = minefield
        minefieldScreen?.bindMinefield(minefield)
    }

    private fun refreshField() {
        boundAreas?.forEach {
            minefieldScreen?.addActor(it)
        }
    }

    fun bindField(field: List<Area>) {
        boundAreas = field.map {
            AreaActor(size = areaSize, area = it)
        }
        refreshField()
    }

    fun setActionsEnabled(enabled: Boolean) {

    }
}
