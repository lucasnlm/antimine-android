package dev.lucasnlm.antimine.common.level.utils

import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ClockManagerTest {
    private val dispatcher = StandardTestDispatcher()

    @Test
    fun `test time before start`() =
        runTest(dispatcher) {
            // Given
            val clockManager = ClockManager(this)

            // When
            dispatcher.scheduler.advanceTimeBy(500L)

            // Then
            assertEquals(0L, clockManager.timeInSeconds())
        }

    @Test
    fun `test time passing more then 1s`() =
        runTest(dispatcher) {
            // Given
            val clockManager = ClockManager(this)

            // When
            clockManager.start()
            dispatcher.scheduler.advanceTimeBy(1500L)

            // Then
            clockManager.stop()
            assertEquals(1L, clockManager.timeInSeconds())
        }

    @Test
    fun `test time passing less then 1s`() =
        runTest(dispatcher) {
            // Given
            val clockManager = ClockManager(this)

            // When
            clockManager.start()
            dispatcher.scheduler.advanceTimeBy(500L)

            // Then
            clockManager.stop()
            assertEquals(0L, clockManager.timeInSeconds())
        }

    @Test
    fun `test time passing less then 5s`() =
        runTest(dispatcher) {
            // Given
            val clockManager = ClockManager(this)

            // When
            clockManager.start()
            dispatcher.scheduler.advanceTimeBy(5500L)

            // Then
            clockManager.stop()
            assertEquals(5L, clockManager.timeInSeconds())
        }

    @Test
    fun `test time passing during 5s`() =
        runTest(dispatcher) {
            // Given
            val clockManager = ClockManager(this)

            // When
            clockManager.start()
            val timeCollected = mutableListOf<Long>()
            launch {
                clockManager.observe().take(6).toList(timeCollected)
            }

            dispatcher.scheduler.advanceTimeBy(5500L)

            // Then
            clockManager.stop()
            assertEquals(
                listOf(0L, 1L, 2L, 3L, 4L, 5L),
                timeCollected,
            )
        }
}
