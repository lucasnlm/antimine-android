package dev.lucasnlm.antimine.gdx.controller

import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector2

class GameInputController(
    private val onChangeZoom: (Float) -> Unit,
) : GestureDetector.GestureListener {

    override fun touchDown(
        x: Float,
        y: Float,
        pointer: Int,
        button: Int,
    ): Boolean {
        return false
    }

    override fun tap(
        x: Float,
        y: Float,
        count: Int,
        button: Int,
    ): Boolean {
        return false
    }

    override fun longPress(
        x: Float,
        y: Float,
    ): Boolean {
        return false
    }

    override fun fling(
        velocityX: Float,
        velocityY: Float,
        button: Int,
    ): Boolean {
        return false
    }

    override fun pan(
        x: Float,
        y: Float,
        deltaX: Float,
        deltaY: Float,
    ): Boolean {
        return false
    }

    override fun panStop(
        x: Float,
        y: Float,
        pointer: Int,
        button: Int,
    ): Boolean {
        return false
    }

    override fun zoom(
        initialDistance: Float,
        distance: Float,
    ): Boolean {
        onChangeZoom(initialDistance / distance)
        return true
    }

    override fun pinch(
        initialPointer1: Vector2?,
        initialPointer2: Vector2?,
        pointer1: Vector2?,
        pointer2: Vector2?,
    ): Boolean {
        return false
    }

    override fun pinchStop() {
        // Empty
    }
}
