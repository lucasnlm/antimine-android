package dev.lucasnlm.antimine.gdx.stages

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import dev.lucasnlm.antimine.gdx.actors.AreaActor
import dev.lucasnlm.antimine.gdx.events.GdxEvent

class GameInputListener(
    private val onInputEvent: (GdxEvent) -> Unit,
) : InputListener() {
    override fun touchUp(
        event: InputEvent,
        x: Float,
        y: Float,
        pointer: Int,
        button: Int,
    ) {
        super.touchUp(event, x, y, pointer, button)

        when (event.target) {
            is AreaActor -> {
                val areaActor = event.target as AreaActor
                areaActor.area?.let {
                    onInputEvent(GdxEvent.TouchUpEvent(it.id))
                    areaActor.isPressed = false
                    areaActor.toBack()
                    Gdx.graphics.requestRendering()
                }
            }
        }
    }

    override fun touchDown(
        event: InputEvent,
        x: Float,
        y: Float,
        pointer: Int,
        button: Int,
    ): Boolean {
        when (event.target) {
            is Group -> {
                event.cancel()
            }
            is AreaActor -> {
                val areaActor = (event.target as AreaActor)
                areaActor.area?.let {
                    areaActor.toFront()
                    areaActor.isPressed = true
                    onInputEvent(GdxEvent.TouchDownEvent(it.id))
                    Gdx.graphics.requestRendering()
                }
            }
        }

        return true
    }
}
