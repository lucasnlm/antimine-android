package dev.lucasnlm.antimine.common.level.logic

import android.util.DisplayMetrics
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepositoryImpl
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import org.junit.Assert.assertEquals
import org.junit.Test

class MinefieldFactoryTest {
    private val dimensionRepository: DimensionRepository = mock()
    private val preferencesRepository: PreferencesRepository = mock()

    @Test
    fun testFromDifficultyPresetBeginner() {
        MinefieldRepositoryImpl().fromDifficulty(
            Difficulty.Beginner,
            dimensionRepository,
            preferencesRepository,
        ).run {
            assertEquals(9, width)
            assertEquals(9, height)
            assertEquals(10, mines)
        }
    }

    @Test
    fun testFromDifficultyPresetIntermediate() {
        MinefieldRepositoryImpl().fromDifficulty(
            Difficulty.Intermediate,
            dimensionRepository,
            preferencesRepository,
        ).run {
            assertEquals(16, width)
            assertEquals(16, height)
            assertEquals(40, mines)
        }
    }

    @Test
    fun testFromDifficultyPresetExpert() {
        MinefieldRepositoryImpl().fromDifficulty(
            Difficulty.Expert,
            dimensionRepository,
            preferencesRepository,
        ).run {
            assertEquals(24, width)
            assertEquals(24, height)
            assertEquals(99, mines)
        }
    }

    @Test
    fun testFromDifficultyPresetCustom() {
        val preferencesRepository: PreferencesRepository =
            mock {
                on { customGameMode() } doReturn
                    Minefield(
                        10,
                        10,
                        30,
                    )
            }

        MinefieldRepositoryImpl().fromDifficulty(
            Difficulty.Custom,
            mock(),
            preferencesRepository,
        ).run {
            assertEquals(10, width)
            assertEquals(10, height)
            assertEquals(30, mines)
        }
    }

    @Test
    fun testFromDifficultyPresetStandard() {
        val dimensionRepository: DimensionRepository =
            mock {
                on { areaSize() } doReturn 10.0f
                on { actionBarSizeWithStatus() } doReturn 10
                on { displayMetrics() } doReturn
                    DisplayMetrics().apply {
                        widthPixels = 500
                        heightPixels = 1000
                    }
            }

        MinefieldRepositoryImpl().fromDifficulty(
            Difficulty.Standard,
            dimensionRepository,
            preferencesRepository,
        ).run {
            assertEquals(49, width)
            assertEquals(96, height)
            assertEquals(846, mines)
        }
    }
}
