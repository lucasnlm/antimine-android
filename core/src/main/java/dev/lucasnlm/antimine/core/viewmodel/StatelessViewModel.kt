package dev.lucasnlm.antimine.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

open class StatelessViewModel<Event> : ViewModel() {
    private val eventBroadcast = ConflatedBroadcastChannel<Event>()
    private val sideEffectBroadcast = ConflatedBroadcastChannel<Event>()

    init {
        viewModelScope.launch {
            observeEvent().collect {
                onEvent(it)
            }
        }
    }

    fun sendEvent(event: Event) {
        eventBroadcast.offer(event)
    }

    protected fun sendSideEffect(event: Event) {
        sideEffectBroadcast.offer(event)
    }

    protected open fun onEvent(event: Event) { }

    override fun onCleared() {
        super.onCleared()
        eventBroadcast.close()
        sideEffectBroadcast.close()
    }

    open fun observeEvent(): Flow<Event> = eventBroadcast.asFlow()

    open fun observeSideEffects(): Flow<Event> = sideEffectBroadcast.asFlow()
}
