package dev.lucasnlm.antimine

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.widget.Toast
import androidx.core.content.FileProvider
import dev.lucasnlm.antimine.common.level.data.Area
import dev.lucasnlm.antimine.common.level.data.LevelSetup
import dev.lucasnlm.antimine.common.level.repository.DrawableRepository
import dev.lucasnlm.antimine.common.level.view.AreaPaintSettings
import dev.lucasnlm.antimine.common.level.view.paintOnCanvas
import java.io.File
import java.io.FileOutputStream

class ShareManager(
    context: Context,
    private val setup: LevelSetup,
    private val field: List<Area>
) {
    private val context: Context = context.applicationContext

    fun share(right: Int, time: Int) {
        val file = createImage()

        if (file != null) {
            shareFile(context, file, right, setup.width * setup.height, time)
        } else {
            Toast.makeText(context, context.getString(R.string.fail_to_share), Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImage(): File? {
        val size = 38f
        val padding = 1f
        val radius = 2f

        val drawableRepository = DrawableRepository()

        val paintSettings = AreaPaintSettings(
            Paint().apply {
                isAntiAlias = true
                isDither = true
                style = Paint.Style.FILL
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            },
            RectF(padding, padding, size - padding, size - padding),
            radius
        )

        val imageWidth = (setup.width * size + padding * 2).toInt()
        val imageHeight = (setup.height * size + padding * 2).toInt()
        val bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawRect(0.0f, 0.0f, imageWidth.toFloat(), imageHeight.toFloat(), Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        })

        for (x in 0 until setup.width) {
            for (y in 0 until setup.height) {
                val area = field[x + y * setup.width]
                canvas.save()
                canvas.translate(x * size + padding, y * size + padding)
                area.paintOnCanvas(
                    context,
                    canvas,
                    isAmbientMode = false,
                    isLowBitAmbient = false,
                    isFocused = false,
                    drawableRepository = drawableRepository,
                    paintSettings = paintSettings,
                    markPadding = 6,
                    minePadding = 1
                )
                canvas.restore()
            }
        }

        return saveToCache(context, bitmap)
    }

    private fun saveToCache(context: Context, bitmap: Bitmap): File? {
        var result: File? = null
        val directory = File(context.cacheDir, "share")

        // Remove any previous shared cache.
        directory.deleteRecursively()


        if (directory.mkdirs() || directory.exists()) {
            val file = File.createTempFile("antimine_", ".png", directory)
            if (file.exists()) {
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 95, FileOutputStream(file))) {
                    result = file
                }
            }
        }

        return result
    }

    private fun shareFile(context: Context, file: File, right: Int, total: Int, time: Int) {
        val imageUri = FileProvider.getUriForFile(context, "dev.lucasnlm.antimine.provider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_body_text, right, total, time))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            type = "image/png"
        }
        context.startActivity(intent)
    }
}
