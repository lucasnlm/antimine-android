package dev.lucasnlm.antimine.control.viewmodel

import com.nhaarman.mockitokotlin2.mock
import dev.lucasnlm.antimine.core.haptic.HapticFeedbackManager
import dev.lucasnlm.antimine.preferences.PreferencesRepository
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
    private val hapticFeedbackManager: HapticFeedbackManager = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialValue() {
        val preferenceRepository: PreferencesRepository =
            mockk {
                every { controlStyle() } returns ControlStyle.DoubleClick
                every { touchSensibility() } returns 10
                every { customLongPressTimeout() } returns 500L
                every { getDoubleClickTimeout() } returns 500L
            }

        val viewModel = ControlViewModel(preferenceRepository, hapticFeedbackManager)
        assertEquals(ControlStyle.DoubleClick, viewModel.singleState().selected)
    }

    @Test
    fun testControlStyleChanges() {
        val preferenceRepository: PreferencesRepository =
            mockk(relaxed = true) {
                every { controlStyle() } returns ControlStyle.Standard
            }

        val viewModel = ControlViewModel(preferenceRepository, hapticFeedbackManager)
        viewModel.sendEvent(
            ControlEvent.SelectControlStyle(
                ControlStyle.FastFlag,
            ),
        )
        assertEquals(ControlStyle.FastFlag, viewModel.singleState().selected)
        verify { preferenceRepository.useControlStyle(ControlStyle.FastFlag) }
    }
}
