package dev.lucasnlm.antimine.share

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.data.Area
import dev.lucasnlm.antimine.common.level.data.LevelSetup
import dev.lucasnlm.antimine.common.level.data.Mark
import dev.lucasnlm.antimine.common.level.model.AreaPalette
import dev.lucasnlm.antimine.common.level.repository.DrawableRepository
import dev.lucasnlm.antimine.common.level.view.AreaPaintSettings
import dev.lucasnlm.antimine.common.level.view.paintOnCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ShareBuilder(
    context: Context
) {
    private val context: Context = context.applicationContext

    suspend fun share(levelSetup: LevelSetup, field: List<Area>, spentTime: Long?): Boolean {
        val rightMines = field.count { it.hasMine && it.mark == Mark.Flag }
        val totalMines = field.count { it.hasMine }

        val file = createImage(levelSetup, field)

        return if (file != null) {
            shareFile(context, file, rightMines, totalMines, spentTime)
        } else {
            false
        }
    }

    private suspend fun createImage(levelSetup: LevelSetup, field: List<Area>): File? = withContext(Dispatchers.IO) {
        val size = 38f
        val padding = 1f
        val radius = 2f

        val areaPalette = AreaPalette.fromLightTheme()
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

        val imageWidth = (levelSetup.width * size + padding * 2).toInt()
        val imageHeight = (levelSetup.height * size + padding * 2).toInt()
        val bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawRect(0.0f, 0.0f, imageWidth.toFloat(), imageHeight.toFloat(), Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        })

        for (x in 0 until levelSetup.width) {
            for (y in 0 until levelSetup.height) {
                val area = field[x + y * levelSetup.width]
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
                    minePadding = 1,
                    areaPalette = areaPalette
                )
                canvas.restore()
            }
        }

        saveToCache(context, bitmap)
    }

    private fun saveToCache(context: Context, bitmap: Bitmap): File? {
        var result: File? = null
        val directory = File(context.cacheDir, "share")

        // Remove any previous shared image.
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

    private fun shareFile(context: Context, file: File, right: Int, total: Int, spentTime: Long?): Boolean {
        val imageUri = FileProvider.getUriForFile(context, context.getString(R.string.app_file_provider), file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
            if (spentTime != null) {
                putExtra(
                    Intent.EXTRA_TEXT,
                    context.getString(R.string.share_body_text, right, total, spentTime.toInt())
                )
            } else {
                putExtra(
                    Intent.EXTRA_TEXT,
                    context.getString(
                        R.string.share_body_text_generic,
                        context.getString(R.string.app_name)
                    )
                )
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            type = "image/png"
        }

        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
