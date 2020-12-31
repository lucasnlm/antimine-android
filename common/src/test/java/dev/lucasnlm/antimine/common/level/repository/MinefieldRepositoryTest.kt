package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class MinefieldRepositoryTest {
    private val beginnerMinefield = Minefield(9, 9, 10)
    private val intermediateMinefield = Minefield(16, 16, 40)
    private val expertMinefield = Minefield(24, 24, 99)

    @Test
    fun testStandardSizeCalcWithoutNavigationBar() {
        val minefieldRepository = MinefieldRepository()
        val preferencesRepository = mockk<IPreferencesRepository>(relaxed = true) {
            every { getProgressiveValue() } returns 0
        }
        val dimensionRepository = mockk<IDimensionRepository>(relaxed = true) {
            every { areaSize() } returns 10.0f
            every { navigationBarHeight() } returns 0
            every { displaySize() } returns Size(1000, 1000)
            every { defaultAreaSize() } returns 10.0f
        }

        val minefield = minefieldRepository.fromDifficulty(
            Difficulty.Standard,
            dimensionRepository,
            preferencesRepository
        )

        assertEquals(Minefield(99, 96, 1900), minefield)
    }

    @Test
    fun testStandardSizeCalcWithNavigationBar() {
        val minefieldRepository = MinefieldRepository()
        val preferencesRepository = mockk<IPreferencesRepository>(relaxed = true) {
            every { getProgressiveValue() } returns 0
        }
        val dimensionRepository = mockk<IDimensionRepository>(relaxed = true) {
            every { areaSize() } returns 10.0f
            every { navigationBarHeight() } returns 100
            every { displaySize() } returns Size(1000, 1000)
            every { defaultAreaSize() } returns 10.0f
        }

        val minefield = minefieldRepository.fromDifficulty(
            Difficulty.Standard,
            dimensionRepository,
            preferencesRepository
        )

        assertEquals(Minefield(99, 97, 1920), minefield)
    }

    @Test
    fun testStandardSizeCalcWithNavigationBarAndProgress() {
        val minefieldRepository = MinefieldRepository()
        val preferencesRepository = mockk<IPreferencesRepository>(relaxed = true) {
            every { getProgressiveValue() } returns 50
        }
        val dimensionRepository = mockk<IDimensionRepository>(relaxed = true) {
            every { areaSize() } returns 10.0f
            every { navigationBarHeight() } returns 100
            every { displaySize() } returns Size(1000, 1000)
            every { defaultAreaSize() } returns 10.0f
        }

        val minefield = minefieldRepository.fromDifficulty(
            Difficulty.Standard,
            dimensionRepository,
            preferencesRepository
        )

        assertEquals(Minefield(99, 97, 1920 + 50), minefield)
    }

    @Test
    fun testStandardSizeCalcWithNavigationBarAndHighProgress() {
        val minefieldRepository = MinefieldRepository()
        val preferencesRepository = mockk<IPreferencesRepository>(relaxed = true) {
            every { getProgressiveValue() } returns 10000
        }
        val dimensionRepository = mockk<IDimensionRepository>(relaxed = true) {
            every { areaSize() } returns 10.0f
            every { navigationBarHeight() } returns 100
            every { displaySize() } returns Size(1000, 1000)
            every { defaultAreaSize() } returns 10.0f
        }

        val minefield = minefieldRepository.fromDifficulty(
            Difficulty.Standard,
            dimensionRepository,
            preferencesRepository
        )

        assertEquals(Minefield(99, 97, 4321), minefield)
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
    fun testExpertMinefieldMinefield() {
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
    fun testCustomMinefieldMinefield() {
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
