package dev.lucasnlm.antimine.gdx

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Color.WHITE
import com.badlogic.gdx.graphics.Color.argb8888ToColor
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor

object GdxExt {
    fun Batch.drawRegion(
        texture: TextureRegion,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        blend: Boolean,
        color: Color? = null,
    ) {
        if (blend && !isBlendingEnabled) {
            enableBlending()
        }

        setColor(color ?: WHITE)
        draw(texture, x, y, width, height)
    }

    fun Actor.drawAsset(
        batch: Batch,
        texture: TextureRegion,
        color: Color? = null,
        blend: Boolean = true,
        scale: Float = 1.0f,
    ) {
        if (blend && !batch.isBlendingEnabled) {
            batch.enableBlending()
        }

        batch.run {
            setColor(color ?: WHITE)
            draw(
                texture,
                x - width * (scale - 1.0f) * 0.5f,
                y - height * (scale - 1.0f) * 0.5f,
                width * scale,
                height * scale,
            )
        }
    }

    fun Int.toGdxColor(alpha: Float? = 1.0f): Color {
        val color = Color()
        argb8888ToColor(color, this)
        color.a = alpha ?: 1.0f
        return color
    }

    fun Int.toInverseBackOrWhite(alpha: Float? = 1.0f): Color {
        val sumRgb = (
            android.graphics.Color.red(this) +
                android.graphics.Color.green(this) +
                android.graphics.Color.blue(this)
        )

        val value =
            if (sumRgb > (160 * 3)) {
                0.15f
            } else {
                1.0f
            }

        return Color(value, value, value, alpha ?: 1.0f)
    }

    fun Color.alpha(newAlpha: Float): Color {
        a = newAlpha
        return this
    }

    fun Color.dim(value: Float): Color {
        r *= value
        g *= value
        b *= value
        return this
    }
}
