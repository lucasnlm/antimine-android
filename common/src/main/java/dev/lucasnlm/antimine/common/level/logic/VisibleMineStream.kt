package dev.lucasnlm.antimine.common.level.logic

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class VisibleMineStream {
    private val visibleMinesFlow =
        MutableSharedFlow<Set<Int>>(
            extraBufferCapacity = 1,
        )
    private val requestVisibleMinesFlow =
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 1,
        )

    /** Returns a [SharedFlow] that emits the visible mines. */
    fun observeVisibleMines() = visibleMinesFlow.asSharedFlow()

    /** Updates the visible mines. */
    fun update(visibleMines: Set<Int>) {
        visibleMinesFlow.tryEmit(visibleMines)
    }

    /** Requests the visible mines. */
    suspend fun requestVisibleMines() {
        requestVisibleMinesFlow.emit(Unit)
    }

    /** Returns a [SharedFlow] that emits when the visible mines are requested. */
    fun observeRequestVisibleMines() = requestVisibleMinesFlow.asSharedFlow()
}
