package dev.lucasnlm.antimine.custom

import dev.lucasnlm.antimine.IntentViewModelTest
import dev.lucasnlm.antimine.custom.viewmodel.CreateGameViewModel
import dev.lucasnlm.antimine.custom.viewmodel.CustomEvent
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateGameViewModelTest : IntentViewModelTest() {
    @Test
    fun testInitialValue() {
        val preferenceRepository: PreferencesRepository =
            mockk {
                every { customGameMode() } returns Minefield(10, 12, 9)
                every { forgetCustomSeed() } returns Unit
            }

        val result = CreateGameViewModel(preferenceRepository).singleState()

        assertEquals(10, result.width)
        assertEquals(12, result.height)
        assertEquals(9, result.mines)
    }

    @Test
    fun testSetNewCustomValues() {
        val preferenceRepository: PreferencesRepository =
            mockk {
                every { customGameMode() } returns Minefield(10, 12, 9)
                every { updateCustomGameMode(any()) } returns Unit
                every { forgetCustomSeed() } returns Unit
            }

        val result =
            CreateGameViewModel(preferenceRepository).run {
                sendEvent(CustomEvent.UpdateCustomGameEvent(Minefield(9, 8, 5)))
                singleState()
            }

        assertEquals(9, result.width)
        assertEquals(8, result.height)
        assertEquals(5, result.mines)
    }
}
