package dev.lucasnlm.antimine.control.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import dev.lucasnlm.antimine.IntentViewModelTest
import dev.lucasnlm.antimine.core.control.ControlStyle
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ControlViewModelTest : IntentViewModelTest() {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private fun ControlViewModel.selectedControlStyle() = singleState().let {
        it.gameControls[it.selectedIndex].controlStyle
    }

    @Test
    fun testInitialValue() {
        val preferenceRepository: IPreferencesRepository = mockk {
            every { controlStyle() } returns ControlStyle.DoubleClick
        }

        val viewModel = ControlViewModel(preferenceRepository)
        assertEquals(ControlStyle.DoubleClick, viewModel.selectedControlStyle())
    }

    @Test
    fun testControlStyleChanges() {
        val preferenceRepository: IPreferencesRepository = mockk(relaxed = true) {
            every { controlStyle() } returns ControlStyle.Standard
        }

        val viewModel = ControlViewModel(preferenceRepository)
        viewModel.sendEvent(ControlEvent.SelectControlStyle(ControlStyle.FastFlag))
        assertEquals(ControlStyle.FastFlag, viewModel.selectedControlStyle())
        verify { preferenceRepository.useControlStyle(ControlStyle.FastFlag) }
    }
}
