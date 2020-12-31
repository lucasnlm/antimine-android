package dev.lucasnlm.antimine.control

import dev.lucasnlm.antimine.IntentViewModelTest
import dev.lucasnlm.antimine.control.viewmodel.ControlEvent
import dev.lucasnlm.antimine.control.viewmodel.ControlViewModel
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class ControlViewModelTest : IntentViewModelTest() {
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
        viewModel.sendEvent(
            ControlEvent.SelectControlStyle(
                ControlStyle.FastFlag
            )
        )
        assertEquals(ControlStyle.FastFlag, viewModel.selectedControlStyle())
        verify { preferenceRepository.useControlStyle(ControlStyle.FastFlag) }
    }
}
