package dev.lucasnlm.antimine.common.level.utils

import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Timer

internal class MockClock(
    private val timer: Timer,
) : Clock() {
    override fun provideTimer(): Timer = timer

    fun mockTime(time: Long) {
        elapsedTimeSeconds = time
    }
}

class ClockTest {

    @Test
    fun testClock() {
        val mockTimer = mock<Timer>()
        val clock = MockClock(mockTimer)

        clock.mockTime(0)
        assertEquals(clock.time(), 0L)

        clock.mockTime(5)
        assertEquals(clock.time(), 5L)
    }

    @Test
    fun testClockTeset() {
        val mockTimer = mock<Timer>()
        val clock = MockClock(mockTimer)

        clock.mockTime(5)
        assertEquals(clock.time(), 5L)

        clock.reset()
        assertEquals(clock.time(), 0L)

        clock.reset(10L)
        assertEquals(clock.time(), 10L)
    }

    @Test
    fun testStop() {
        val mockTimer = mock<Timer>()
        val clock = MockClock(mockTimer)

        clock.start { }
        assertFalse(clock.isStopped)
        clock.stop()
        assertTrue(clock.isStopped)
    }
}
