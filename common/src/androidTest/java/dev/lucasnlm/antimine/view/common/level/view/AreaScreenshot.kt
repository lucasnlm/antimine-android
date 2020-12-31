package dev.lucasnlm.antimine.view.common.level.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.test.platform.app.InstrumentationRegistry
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Mark
import dev.lucasnlm.antimine.ui.repository.Themes.LightTheme
import dev.lucasnlm.antimine.ui.view.createAreaPaintSettings
import dev.lucasnlm.antimine.ui.view.paintOnCanvas
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class AreaScreenshot {
    private lateinit var context: Context

    private fun saveImage(area: Area, fileName: String, ambientMode: Boolean): File {
        val paintSettings = createAreaPaintSettings(context, 128.0f, 3)
        val size = paintSettings.rectF.width().toInt()
        val testPadding = 4
        val bitmap = Bitmap.createBitmap(size + testPadding, size + testPadding, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)

        canvas.drawRect(
            0.0f,
            0.0f,
            size.toFloat() + testPadding,
            size.toFloat() + testPadding,
            Paint().apply {
                color = if (ambientMode) Color.BLACK else Color.WHITE
                style = Paint.Style.FILL
            }
        )

        canvas.save()
        canvas.translate(testPadding * 0.5f, testPadding.toFloat() * 0.5f)
        area.paintOnCanvas(
            context,
            canvas,
            isAmbientMode = ambientMode,
            isLowBitAmbient = false,
            isFocused = false,
            paintSettings = paintSettings,
            theme = LightTheme
        )
        canvas.restore()

        val file = File(context.filesDir, fileName)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(file))
        return file
    }

    private fun compareScreenshot(current: File, fileName: String): Boolean {
        val referenceInput = javaClass.getResourceAsStream("/area/$fileName")
        var result = false

        if (referenceInput != null) {
            val referenceBytes = referenceInput.readBytes()
            val currentBytes = current.inputStream().readBytes()

            if (referenceBytes.size == currentBytes.size) {
                result = referenceBytes.contentEquals(currentBytes)
            }
        }

        return result
    }

    private fun screenshotTest(area: Area, fileName: String, ambientMode: Boolean = false) {
        val current = saveImage(area, fileName, ambientMode)
        assertTrue("$fileName doesn't match the reference", compareScreenshot(current, fileName))
    }

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun testCoveredArea() {
        val area = Area(0, 0, 0, isCovered = true)
        screenshotTest(area, "covered.png")
    }

    @Test
    fun testCoveredAreaAmbientMode() {
        val area = Area(0, 0, 0, isCovered = true)
        screenshotTest(area, "covered_ambient.png", true)
    }

    @Test
    fun testUncoveredArea() {
        val area = Area(0, 0, 0, isCovered = false)
        screenshotTest(area, "uncovered.png")
    }

    @Test
    fun testUncoveredAreaAmbientMode() {
        val area = Area(0, 0, 0, isCovered = false)
        screenshotTest(area, "uncovered_ambient.png", ambientMode = true)
    }

    @Test
    fun testUncoveredAreaWithTips() {
        repeat(8) {
            val id = it + 1
            screenshotTest(
                Area(
                    0,
                    0,
                    0,
                    isCovered = false,
                    minesAround = id
                ),
                "mines_around_$id.png"
            )
        }
    }

    @Test
    fun testUncoveredAreaWithTipsAmbientMode() {
        repeat(8) {
            val id = it + 1
            screenshotTest(
                Area(
                    0,
                    0,
                    0,
                    isCovered = false,
                    minesAround = id
                ),
                "mines_around_${id}_ambient.png",
                true
            )
        }
    }

    @Test
    fun testCoveredAreaWithFlag() {
        val area = Area(
            0,
            0,
            0,
            isCovered = true,
            mark = Mark.Flag
        )
        screenshotTest(area, "covered_flag.png")
    }

    @Test
    fun testCoveredAreaWithFlagAmbient() {
        val area = Area(
            0,
            0,
            0,
            isCovered = true,
            mark = Mark.Flag
        )
        screenshotTest(area, "covered_flag_ambient.png", true)
    }

    @Test
    fun testCoveredAreaWithQuestion() {
        val area = Area(
            0,
            0,
            0,
            isCovered = true,
            mark = Mark.Question
        )
        screenshotTest(area, "covered_question.png")
    }

    @Test
    fun testCoveredAreaWithQuestionAmbient() {
        val area = Area(
            0,
            0,
            0,
            isCovered = true,
            mark = Mark.Question
        )
        screenshotTest(area, "covered_question_ambient.png", true)
    }

    @Test
    fun testCoveredAreaHighlighted() {
        val area = Area(
            0,
            0,
            0,
            isCovered = true,
            highlighted = true
        )
        screenshotTest(area, "covered_highlighted.png")
    }

    @Test
    fun testCoveredAreaHighlightedAmbient() {
        val area = Area(
            0,
            0,
            0,
            isCovered = true,
            highlighted = true
        )
        screenshotTest(area, "covered_highlighted_ambient.png", true)
    }

    @Test
    fun testCoveredAreaWithMine() {
        val area = Area(
            0,
            0,
            0,
            isCovered = true,
            hasMine = true
        )
        screenshotTest(area, "covered_mine.png")
    }

    @Test
    fun testCoveredAreaWithMineAmbient() {
        val area = Area(
            0,
            0,
            0,
            isCovered = true,
            hasMine = true
        )
        screenshotTest(area, "covered_mine_ambient.png", true)
    }

    @Test
    fun testUncoveredAreaWithMine() {
        val area = Area(
            0,
            0,
            0,
            isCovered = false,
            hasMine = true
        )
        screenshotTest(area, "uncovered_mine.png")
    }

    @Test
    fun testUncoveredAreaWithMineAmbient() {
        val area = Area(
            0,
            0,
            0,
            isCovered = false,
            hasMine = true
        )
        screenshotTest(area, "uncovered_mine_ambient.png", true)
    }

    @Test
    fun testUncoveredAreaHighlighted() {
        val area = Area(
            0,
            0,
            0,
            isCovered = false,
            hasMine = false,
            highlighted = true
        )
        screenshotTest(area, "uncovered_highlighted.png")
    }

    @Test
    fun testUncoveredAreaHighlightedAmbient() {
        val area = Area(
            0,
            0,
            0,
            isCovered = false,
            hasMine = false,
            highlighted = true
        )
        screenshotTest(area, "uncovered_highlighted_ambient.png", true)
    }

    @Test
    fun testUncoveredAreaWithMineExploded() {
        val area = Area(
            0,
            0,
            0,
            isCovered = false,
            hasMine = true,
            mistake = true
        )
        screenshotTest(area, "uncovered_mine_exploded.png")
    }

    @Test
    fun testUncoveredAreaWithMineExplodedAmbient() {
        val area = Area(
            0,
            0,
            0,
            isCovered = false,
            hasMine = true,
            mistake = true
        )
        screenshotTest(area, "uncovered_mine_exploded_ambient.png", true)
    }

    @Test
    fun testUncoveredAreaWithTipsHighlighted() {
        repeat(8) {
            val id = it + 1
            screenshotTest(
                Area(
                    0,
                    0,
                    0,
                    isCovered = false,
                    minesAround = id,
                    highlighted = true
                ),
                "mines_around_highlighted_$id.png"
            )
        }
    }

    @Test
    fun testUncoveredAreaWithTipsHighlightedAmbient() {
        repeat(8) {
            val id = it + 1
            screenshotTest(
                Area(
                    0,
                    0,
                    0,
                    isCovered = false,
                    minesAround = id,
                    highlighted = true
                ),
                "mines_around_highlighted_${id}_ambient.png",
                true
            )
        }
    }
}
