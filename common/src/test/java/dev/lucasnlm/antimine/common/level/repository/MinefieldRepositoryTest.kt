package dev.lucasnlm.antimine.common.level.repository

import android.util.DisplayMetrics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MinefieldRepositoryTest {
    private val beginnerMinefield = Minefield(9, 9, 10)
    private val intermediateMinefield = Minefield(16, 16, 40)
    private val expertMinefield = Minefield(24, 24, 99)
    private val masterMinefield = Minefield(50, 50, 450)
    private val legendMinefield = Minefield(100, 100, 2000)

    @Test
    fun testStandardSizeCalcWithoutNavigationBar() {
        val minefieldRepository = MinefieldRepositoryImpl()
        val preferencesRepository =
            mockk<PreferencesRepository>(relaxed = true) {
                every { getProgressiveValue() } returns 0
            }
        val dimensionRepository =
            mockk<DimensionRepository>(relaxed = true) {
                every { areaSize() } returns 10.0f
                every { verticalNavigationBarHeight() } returns 0
                every { displayMetrics() } returns
                    DisplayMetrics().apply {
                        widthPixels = 1000
                        heightPixels = 1000
                    }
            }

        val minefield =
            minefieldRepository.fromDifficulty(
                Difficulty.Standard,
                dimensionRepository,
                preferencesRepository,
            )

        assertEquals(Minefield(99, 96, 1710), minefield)
    }

    @Test
    fun testStandardSizeCalcWithNavigationBar() {
        val minefieldRepository = MinefieldRepositoryImpl()
        val preferencesRepository =
            mockk<PreferencesRepository>(relaxed = true) {
                every { getProgressiveValue() } returns 0
            }
        val dimensionRepository =
            mockk<DimensionRepository>(relaxed = true) {
                every { areaSize() } returns 10.0f
                every { verticalNavigationBarHeight() } returns 100
                every { displayMetrics() } returns
                    DisplayMetrics().apply {
                        widthPixels = 1000
                        heightPixels = 1000
                    }
            }

        val minefield =
            minefieldRepository.fromDifficulty(
                Difficulty.Standard,
                dimensionRepository,
                preferencesRepository,
            )

        assertEquals(Minefield(99, 97, 1728), minefield)
    }

    @Test
    fun testStandardSizeCalcWithNavigationBarAndProgress() {
        val minefieldRepository = MinefieldRepositoryImpl()
        val preferencesRepository =
            mockk<PreferencesRepository>(relaxed = true) {
                every { getProgressiveValue() } returns 50
            }
        val dimensionRepository =
            mockk<DimensionRepository>(relaxed = true) {
                every { areaSize() } returns 10.0f
                every { verticalNavigationBarHeight() } returns 100
                every { displayMetrics() } returns
                    DisplayMetrics().apply {
                        widthPixels = 1000
                        heightPixels = 1000
                    }
            }

        val minefield =
            minefieldRepository.fromDifficulty(
                Difficulty.Standard,
                dimensionRepository,
                preferencesRepository,
            )

        assertEquals(Minefield(99, 97, 1778), minefield)
    }

    @Test
    fun testStandardSizeCalcWithNavigationBarAndHighProgress() {
        val minefieldRepository = MinefieldRepositoryImpl()
        val preferencesRepository =
            mockk<PreferencesRepository>(relaxed = true) {
                every { getProgressiveValue() } returns 10000
            }
        val dimensionRepository =
            mockk<DimensionRepository>(relaxed = true) {
                every { areaSize() } returns 10.0f
                every { verticalNavigationBarHeight() } returns 100
                every { displayMetrics() } returns
                    DisplayMetrics().apply {
                        widthPixels = 1000
                        heightPixels = 1000
                    }
            }

        val minefield =
            minefieldRepository.fromDifficulty(
                Difficulty.Standard,
                dimensionRepository,
                preferencesRepository,
            )

        assertEquals(Minefield(233, 231, 11728), minefield)
    }

    @Test
    fun testBeginnerMinefield() {
        val minefieldRepository = MinefieldRepositoryImpl()
        val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
        val dimensionRepository = mockk<DimensionRepository>(relaxed = true)

        val minefield =
            minefieldRepository.fromDifficulty(
                Difficulty.Beginner,
                dimensionRepository,
                preferencesRepository,
            )
        assertEquals(beginnerMinefield, minefield)
    }

    @Test
    fun testIntermediateMinefield() {
        val minefieldRepository = MinefieldRepositoryImpl()
        val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
        val dimensionRepository = mockk<DimensionRepository>(relaxed = true)

        val minefield =
            minefieldRepository.fromDifficulty(
                Difficulty.Intermediate,
                dimensionRepository,
                preferencesRepository,
            )
        assertEquals(intermediateMinefield, minefield)
    }

    @Test
    fun testExpertMinefield() {
        val minefieldRepository = MinefieldRepositoryImpl()
        val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
        val dimensionRepository = mockk<DimensionRepository>(relaxed = true)

        val minefield =
            minefieldRepository.fromDifficulty(
                Difficulty.Expert,
                dimensionRepository,
                preferencesRepository,
            )
        assertEquals(expertMinefield, minefield)
    }

    @Test
    fun testMasterMinefield() {
        val minefieldRepository = MinefieldRepositoryImpl()
        val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
        val dimensionRepository = mockk<DimensionRepository>(relaxed = true)

        val minefield =
            minefieldRepository.fromDifficulty(
                Difficulty.Master,
                dimensionRepository,
                preferencesRepository,
            )
        assertEquals(masterMinefield, minefield)
    }

    @Test
    fun testLegendMinefield() {
        val minefieldRepository = MinefieldRepositoryImpl()
        val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
        val dimensionRepository = mockk<DimensionRepository>(relaxed = true)

        val minefield =
            minefieldRepository.fromDifficulty(
                Difficulty.Legend,
                dimensionRepository,
                preferencesRepository,
            )
        assertEquals(legendMinefield, minefield)
    }

    @Test
    fun testCustomMinefield() {
        val minefieldRepository = MinefieldRepositoryImpl()
        val preferencesRepository =
            mockk<PreferencesRepository>(relaxed = true) {
                every { customGameMode() } returns Minefield(25, 20, 12)
            }
        val dimensionRepository = mockk<DimensionRepository>(relaxed = true)

        val minefield =
            minefieldRepository.fromDifficulty(
                Difficulty.Custom,
                dimensionRepository,
                preferencesRepository,
            )
        assertEquals(Minefield(25, 20, 12), minefield)
    }

    @Test
    fun testDifficultyRatio() {
        val minefieldRepository = MinefieldRepositoryImpl()
        val preferencesRepository = mockk<PreferencesRepository>(relaxed = true) {}
        val dimensionRepository = mockk<DimensionRepository>(relaxed = true)

        val ratios =
            mapOf(
                Difficulty.Beginner to 12,
                Difficulty.Intermediate to 15,
                Difficulty.Expert to 17,
                Difficulty.Master to 18,
                Difficulty.Legend to 20,
            )

        ratios.forEach {
            assertEquals(
                it.value,
                minefieldRepository.fromDifficulty(
                    it.key,
                    dimensionRepository,
                    preferencesRepository,
                ).ratioPercent(),
                "${it.key} should have ratio of ${it.value}",
            )
        }

        ratios.entries.sortedBy {
            it.key.ordinal
        }.reduce { previous, current ->
            assertTrue(
                current.value > previous.value,
                "${current.key} must have a ratio greater than ${previous.key}",
            )
            current
        }
    }
}
