package dev.lucasnlm.antimine.gdx.actors

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

fun ShapeRenderer.roundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float) {
    // Central rectangle
    rect(x + radius, y + radius, width - 2 * radius, height - 2 * radius)

    // Four side rectangles, in clockwise order
    rect(x + radius, y, width - 2 * radius, radius)
    rect(x + width - radius, y + radius, radius, height - 2 * radius)
    rect(x + radius, y + height - radius, width - 2 * radius, radius)
    rect(x, y + radius, radius, height - 2 * radius)

    // Four arches, clockwise too
    arc(x + radius, y + radius, radius, 180f, 90f)
    arc(x + width - radius, y + radius, radius, 270f, 90f)
    arc(x + width - radius, y + height - radius, radius, 0f, 90f)
    arc(x + radius, y + height - radius, radius, 90f, 90f)
}

fun ShapeRenderer.scope(type: ShapeRenderer.ShapeType = ShapeRenderer.ShapeType.Filled, block: ShapeRenderer.() -> Unit) {
    begin(type)
    block()
    end()
}

fun Batch.scope(block: Batch.() -> Unit) {
    end()
    block()
    begin()
}
