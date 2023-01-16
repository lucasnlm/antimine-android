package dev.lucasnlm.antimine.gdx

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import dev.lucasnlm.antimine.gdx.models.GameTextures

// The references are held in public static fields which allows static access to all sub systems. Do not
// use Graphics in a thread that is not the rendering thread.
//
// This is normally a design faux pas but in this case is better than the alternatives.
object GameContext {
    var atlas: TextureAtlas? = null
    var gameTextures: GameTextures? = null
    var zoom = 1.0f

    // Defines if it should tint or not the areas.
    var canTintAreas = true

    // Global alpha used to relate minefield zoom to things that shouldn't be render when zoom out.
    var zoomLevelAlpha: Float = 1.0f

    // Enables / Disables actions according to external logic.
    var actionsEnabled: Boolean = false

    // Predefined theme based colors
    var backgroundColor: Color = Color.BLACK
    var coveredAreaColor: Color = Color.BLACK
    var coveredMarkedAreaColor: Color = Color.BLACK
    val whiteColor: Color = Color.WHITE
    var markColor: Color = Color.WHITE
}
