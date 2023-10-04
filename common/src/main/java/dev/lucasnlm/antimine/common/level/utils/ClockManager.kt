package dev.lucasnlm.antimine.common.level.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Manages the clock.
 */
open class ClockManager(
    private val scope: CoroutineScope,
) {
    private val timeObservable = MutableStateFlow(0L)
    private var currentJob: Job? = null

    val isStopped: Boolean
        get() = currentJob == null

    /**
     * Resets the clock to the given value.
     * @param initialValueInSeconds The initial value in seconds.
     */
    fun reset(initialValueInSeconds: Long = 0L) {
        stop()
        timeObservable.tryEmit(
            initialValueInSeconds * SECOND_IN_MILLIS,
        )
    }

    /**
     * Returns the current time in seconds.
     */
    fun timeInSeconds(): Long {
        return timeObservable.value / SECOND_IN_MILLIS
    }

    /**
     * Stops the clock.
     */
    fun stop() {
        currentJob?.cancel()
        currentJob = null
    }

    /**
     * Returns a flow that emits the current time in seconds.
     */
    fun observe(): Flow<Long> {
        return timeObservable.asStateFlow().map {
            it / SECOND_IN_MILLIS
        }.distinctUntilChanged()
    }

    /**
     * Starts the clock.
     */
    fun start() {
        stop()
        currentJob =
            scope.launch {
                while (isActive) {
                    delay(CLOCK_STEP_MS)
                    timeObservable.emit(timeObservable.value + CLOCK_STEP_MS)
                }
            }
    }

    companion object {
        private const val CLOCK_STEP_MS = 200L
        private const val SECOND_IN_MILLIS = 1000L
    }
}
