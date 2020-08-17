package dev.lucasnlm.antimine.about

import dev.lucasnlm.antimine.IntentViewModelTest
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Test

class AboutViewModelTest : IntentViewModelTest() {
    @Test
    fun testLoad() {
        val viewModel = AboutViewModel(mockk())
        val state = viewModel.singleState()
        assertTrue(state.licenses.isNotEmpty())
        assertTrue(state.translators.isNotEmpty())
    }
}
