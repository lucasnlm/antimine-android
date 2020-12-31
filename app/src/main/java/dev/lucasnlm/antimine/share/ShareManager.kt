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
import android.widget.Toast
import androidx.core.content.FileProvider
import dev.lucasnlm.antimine.BuildConfig
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.AreaPaintSettings
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.ui.view.paintOnCanvas
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ShareManager(
    private val context: Context,
    private val themeRepository: IThemeRepository,
) {
    private suspend fun share(minefield: Minefield, field: List<Area>): Boolean {
        val file = createImage(minefield, field)

        return if (file != null) {
            shareFile(context, file)
        } else {
            false
        }
    }

    private suspend fun createImage(minefield: Minefield, field: List<Area>): File? = withContext(Dispatchers.IO) {
        val size = 38f
        val padding = 1f
        val radius = 2f

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

        val imageWidth = (minefield.width * size + padding * 2).toInt()
        val imageHeight = (minefield.height * size + padding * 2).toInt()
        val bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawRect(
            0.0f,
            0.0f,
            imageWidth.toFloat(),
            imageHeight.toFloat(),
            Paint().apply {
                color = Color.WHITE
                alpha = 0xff
                style = Paint.Style.FILL
            }
        )

        for (x in 0 until minefield.width) {
            for (y in 0 until minefield.height) {
                val area = field.first { it.id == (x + y * minefield.width) }
                canvas.save()
                canvas.translate(x * size + padding, y * size + padding)
                area.paintOnCanvas(
                    context,
                    canvas,
                    theme = themeRepository.getTheme(),
                    isAmbientMode = false,
                    isLowBitAmbient = false,
                    isFocused = false,
                    paintSettings = paintSettings,
                    markPadding = 6,
                    minePadding = 1
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

    private fun shareFile(context: Context, file: File): Boolean {
        val imageUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
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

    suspend fun shareField(minefield: Minefield?, field: List<Area>?) {
        val result = if (minefield != null && field != null && field.count() != 0) {
            share(minefield, field)
        } else {
            false
        }

        if (!result) {
            Toast.makeText(context, context.getString(R.string.fail_to_share), Toast.LENGTH_SHORT).show()
        }
    }
}
