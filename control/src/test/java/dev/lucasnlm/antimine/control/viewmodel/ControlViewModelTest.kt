package dev.lucasnlm.antimine.control.viewmodel

import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ControlViewModelTest {
    private val dispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

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
