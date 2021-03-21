package dev.lucasnlm.antimine.gdx

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import dev.lucasnlm.antimine.gdx.models.GameTextures
import dev.lucasnlm.antimine.gdx.models.TouchAreaAction

// The references are held in public static fields which allows static access to all sub systems. Do not
// use Graphics in a thread that is not the rendering thread.
//
// This is normally a design faux pas but in this case is better than the alternatives.
object GdxLocal {
    var pressedArea: TouchAreaAction? = null
    var textureAtlas: TextureAtlas? = null
    var gameTextures: GameTextures? = null
    var qualityZoomLevel: Int = 0
    var globalAlpha = 1.0f
    var focusResizeLevel = 1.15f
    const val maxFocusResizeLevel = 1.15f
    var hasHighlightAreas = false
    var highlightAlpha = 0.0f
}
