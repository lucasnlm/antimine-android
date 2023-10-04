package dev.lucasnlm.antimine

import androidx.annotation.CallSuper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

abstract class IntentViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @CallSuper
    @Before
    open fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @CallSuper
    @After
    open fun tearDown() {
        Dispatchers.resetMain()
    }
}
