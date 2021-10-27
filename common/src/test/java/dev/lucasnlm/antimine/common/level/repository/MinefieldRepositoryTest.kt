package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.core.repository.Size
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class MinefieldRepositoryTest {
    private val beginnerMinefield = Minefield(9, 9, 10)
    private val intermediateMinefield = Minefield(16, 16, 40)
    private val expertMinefield = Minefield(24, 24, 99)
    private val masterMinefield = Minefield(50, 50, 400)
    private val legendMinefield = Minefield(100, 100, 2000)

    @Test
    fun testStandardSizeCalcWithoutNavigationBar() {
        val minefieldRepository = MinefieldRepository()
        val preferencesRepository = mockk<IPreferencesRepository>(relaxed = true) {
            every { getProgressiveValue() } returns 0
        }
        val dimensionRepository = mockk<IDimensionRepository>(relaxed = true) {
            every { areaSize() } returns 10.0f
            every { verticalNavigationBarHeight() } returns 0
            every { displaySize() } returns Size(1000, 1000)
            every { defaultAreaSize() } returns 10.0f
        }

        val minefield = minefieldRepository.fromDifficulty(
            Difficulty.Standard,
            dimensionRepository,
            preferencesRepository
        )

        assertEquals(Minefield(99, 96, 1710), minefield)
    }

    @Test
    fun testStandardSizeCalcWithNavigationBar() {
        val minefieldRepository = MinefieldRepository()
        val preferencesRepository = mockk<IPreferencesRepository>(relaxed = true) {
            every { getProgressiveValue() } returns 0
        }
        val dimensionRepository = mockk<IDimensionRepository>(relaxed = true) {
            every { areaSize() } returns 10.0f
            every { verticalNavigationBarHeight() } returns 100
            every { displaySize() } returns Size(1000, 1000)
            every { defaultAreaSize() } returns 10.0f
        }

        val minefield = minefieldRepository.fromDifficulty(
            Difficulty.Standard,
            dimensionRepository,
            preferencesRepository
        )

        assertEquals(Minefield(99, 97, 1728), minefield)
    }

    @Test
    fun testStandardSizeCalcWithNavigationBarAndProgress() {
        val minefieldRepository = MinefieldRepository()
        val preferencesRepository = mockk<IPreferencesRepository>(relaxed = true) {
            every { getProgressiveValue() } returns 50
        }
        val dimensionRepository = mockk<IDimensionRepository>(relaxed = true) {
            every { areaSize() } returns 10.0f
            every { verticalNavigationBarHeight() } returns 100
            every { displaySize() } returns Size(1000, 1000)
            every { defaultAreaSize() } returns 10.0f
        }

        val minefield = minefieldRepository.fromDifficulty(
            Difficulty.Standard,
            dimensionRepository,
            preferencesRepository
        )

        assertEquals(Minefield(99, 97, 1778), minefield)
    }

    @Test
    fun testStandardSizeCalcWithNavigationBarAndHighProgress() {
        val minefieldRepository = MinefieldRepository()
        val preferencesRepository = mockk<IPreferencesRepository>(relaxed = true) {
            every { getProgressiveValue() } returns 10000
        }
        val dimensionRepository = mockk<IDimensionRepository>(relaxed = true) {
            every { areaSize() } returns 10.0f
            every { verticalNavigationBarHeight() } returns 100
            every { displaySize() } returns Size(1000, 1000)
            every { defaultAreaSize() } returns 10.0f
        }

        val minefield = minefieldRepository.fromDifficulty(
            Difficulty.Standard,
            dimensionRepository,
            preferencesRepository
        )

        assertEquals(Minefield(139, 137, 4321), minefield)
    }

    @Test
    fun testBeginnerMinefield() {
        val minefieldRepository = MinefieldRepository()
        val preferencesRepository = mockk<IPreferencesRepository>(relaxed = true)
        val dimensionRepository = mockk<IDimensionRepository>(relaxed = true)

        val minefield = minefieldRepository.fromDifficulty(
            Difficulty.Beginner,
            dimensionRepository,
            preferencesRepository
        )
        assertEquals(beginnerMinefield, minefield)
    }

    @Test
    fun testIntermediateMinefield() {
        val minefieldRepository = MinefieldRepository()
        val preferencesRepository = mockk<IPreferencesRepository>(relaxed = true)
        val dimensionRepository = mockk<IDimensionRepository>(relaxed = true)

        val minefield = minefieldRepository.fromDifficulty(
            Difficulty.Intermediate,
            dimensionRepository,
            preferencesRepository
        )
        assertEquals(intermediateMinefield, minefield)
    }

    @Test
    fun testExpertMinefield() {
        val minefieldRepository = MinefieldRepository()
        val preferencesRepository = mockk<IPreferencesRepository>(relaxed = true)
        val dimensionRepository = mockk<IDimensionRepository>(relaxed = true)

        val minefield = minefieldRepository.fromDifficulty(
            Difficulty.Expert,
            dimensionRepository,
            preferencesRepository
        )
        assertEquals(expertMinefield, minefield)
    }

    @Test
    fun testMasterMinefield() {
        val minefieldRepository = MinefieldRepository()
        val preferencesRepository = mockk<IPreferencesRepository>(relaxed = true)
        val dimensionRepository = mockk<IDimensionRepository>(relaxed = true)

        val minefield = minefieldRepository.fromDifficulty(
            Difficulty.Master,
            dimensionRepository,
            preferencesRepository
        )
        assertEquals(masterMinefield, minefield)
    }

    @Test
    fun testLegendMinefield() {
        val minefieldRepository = MinefieldRepository()
        val preferencesRepository = mockk<IPreferencesRepository>(relaxed = true)
        val dimensionRepository = mockk<IDimensionRepository>(relaxed = true)

        val minefield = minefieldRepository.fromDifficulty(
            Difficulty.Legend,
            dimensionRepository,
            preferencesRepository
        )
        assertEquals(legendMinefield, minefield)
    }

    @Test
    fun testCustomMinefield() {
        val minefieldRepository = MinefieldRepository()
        val preferencesRepository = mockk<IPreferencesRepository>(relaxed = true) {
            every { customGameMode() } returns Minefield(25, 20, 12)
        }
        val dimensionRepository = mockk<IDimensionRepository>(relaxed = true)

        val minefield = minefieldRepository.fromDifficulty(
            Difficulty.Custom,
            dimensionRepository,
            preferencesRepository
        )
        assertEquals(Minefield(25, 20, 12), minefield)
    }
}
