package dev.lucasnlm.antimine.about

import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AboutViewModelTest {
    private val dispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoad() {
        val viewModel = AboutViewModel(mockk())
        val state = viewModel.singleState()
        assertTrue(state.licenses.isNotEmpty())
        assertTrue(state.translators.isNotEmpty())
    }
}
