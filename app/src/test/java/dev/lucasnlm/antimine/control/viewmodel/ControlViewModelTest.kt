package dev.lucasnlm.antimine.control.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import dev.lucasnlm.antimine.core.control.ControlStyle
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ControlViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun testInitialValue() {
        val preferenceRepository: IPreferencesRepository = mockk {
            every { controlStyle() } returns ControlStyle.DoubleClick
        }

        val viewModel = ControlViewModel(preferenceRepository)
        assertEquals(ControlStyle.DoubleClick, viewModel.controlTypeSelected.value)
    }

    @Test
    fun testControlStyleChanges() {
        val preferenceRepository: IPreferencesRepository = mockk(relaxed = true) {
            every { controlStyle() } returns ControlStyle.Standard
        }

        val viewModel = ControlViewModel(preferenceRepository)
        viewModel.selectControlType(ControlStyle.FastFlag)
        assertEquals(ControlStyle.FastFlag, viewModel.controlTypeSelected.value)
        verify { preferenceRepository.useControlStyle(ControlStyle.FastFlag) }
    }
}