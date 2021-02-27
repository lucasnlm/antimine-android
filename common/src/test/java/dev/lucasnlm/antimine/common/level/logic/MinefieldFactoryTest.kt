package dev.lucasnlm.antimine.common.level.logic

import android.content.Context
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.core.repository.Size
import dev.lucasnlm.antimine.isPortrait
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import org.junit.Assert.assertEquals
import org.junit.Test

class MinefieldFactoryTest {
    private val dimensionRepository: IDimensionRepository = mock()
    private val preferencesRepository: IPreferencesRepository = mock()
    private val mockContext = mock<Context>().apply {
        whenever(isPortrait()) doReturn true
    }

    @Test
    fun testFromDifficultyPresetBeginner() {
        MinefieldRepository(mockContext).fromDifficulty(
            Difficulty.Beginner,
            dimensionRepository,
            preferencesRepository
        ).run {
            assertEquals(9, width)
            assertEquals(9, height)
            assertEquals(10, mines)
        }
    }

    @Test
    fun testFromDifficultyPresetIntermediate() {
        MinefieldRepository(mockContext).fromDifficulty(
            Difficulty.Intermediate,
            dimensionRepository,
            preferencesRepository
        ).run {
            assertEquals(16, width)
            assertEquals(16, height)
            assertEquals(40, mines)
        }
    }

    @Test
    fun testFromDifficultyPresetExpert() {
        MinefieldRepository(mockContext).fromDifficulty(
            Difficulty.Expert,
            dimensionRepository,
            preferencesRepository
        ).run {
            assertEquals(24, width)
            assertEquals(24, height)
            assertEquals(99, mines)
        }
    }

    @Test
    fun testFromDifficultyPresetCustom() {
        val preferencesRepository: IPreferencesRepository = mock {
            on { customGameMode() } doReturn Minefield(
                10,
                10,
                30
            )
        }

        MinefieldRepository(mockContext).fromDifficulty(
            Difficulty.Custom,
            mock(),
            preferencesRepository
        ).run {
            assertEquals(10, width)
            assertEquals(10, height)
            assertEquals(30, mines)
        }
    }

    @Test
    fun testFromDifficultyPresetStandard() {
        val dimensionRepository: IDimensionRepository = mock {
            on { areaSize() } doReturn 10.0f
            on { defaultAreaSize() } doReturn 10.0f
            on { actionBarSize() } doReturn 10
            on { displaySize() } doReturn Size(500, 1000)
        }

        MinefieldRepository(mockContext).fromDifficulty(
            Difficulty.Standard,
            dimensionRepository,
            preferencesRepository
        ).run {
            assertEquals(49, width)
            assertEquals(96, height)
            assertEquals(940, mines)
        }
    }
}
