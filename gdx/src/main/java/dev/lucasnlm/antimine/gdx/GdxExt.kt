package dev.lucasnlm.antimine.gdx

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Color.WHITE
import com.badlogic.gdx.graphics.Color.argb8888ToColor
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Actor
import dev.lucasnlm.antimine.gdx.models.GameTextures

fun ShapeRenderer.scope(
    type: ShapeRenderer.ShapeType = ShapeRenderer.ShapeType.Filled,
    block: ShapeRenderer.() -> Unit,
) {
    begin(type)
    block()
    end()
}

fun Batch.scope(block: (Batch, GameTextures) -> Unit) {
    GdxLocal.gameTextures?.let {
        end()
        block(this, it)
        begin()
    }
}

fun Batch.drawScope(block: Batch.() -> Unit) {
    begin()
    block()
    end()
}

fun Batch.drawArea(
    texture: Texture,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    blend: Boolean,
    color: Color? = null
) {
    if (blend) {
        enableBlending()
    }

    begin()
    setColor(color ?: WHITE)
    draw(texture, x, y, width, height)
    end()

    if (blend) {
        disableBlending()
    }
}

fun Actor.drawAsset(
    batch: Batch,
    texture: TextureRegion,
    color: Color? = null,
    blend: Boolean = true,
    scale: Float = 1.0f
) {
    if (blend) {
        batch.enableBlending()
    }

    batch.drawScope {
        setColor(color ?: WHITE)
        draw(
            texture,
            x - width * (scale - 1.0f) * 0.5f,
            y - height * (scale - 1.0f) * 0.5f,
            width * scale,
            height * scale
        )
    }

    if (blend) {
        batch.disableBlending()
    }
}

fun Int.toGdxColor(alpha: Float? = 1.0f): Color {
    val color = Color()
    argb8888ToColor(color, this)
    color.a = alpha ?: 1.0f
    return color
}

fun Int.toOppositeMax(alpha: Float? = 1.0f): Color {
    val mid = sequenceOf(
        android.graphics.Color.red(this),
        android.graphics.Color.green(this),
        android.graphics.Color.blue(this),
    ).sum() / 3

    val value = if (mid > 160) {
        0.15f
    } else {
        1.0f
    }

    return Color(value, value, value, alpha ?: 1.0f)
}
