package dev.lucasnlm.antimine.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

open class StatelessViewModel<Event> : ViewModel() {
    private val eventBroadcast = MutableSharedFlow<Event>()
    private val sideEffectBroadcast = MutableSharedFlow<Event>()

    init {
        viewModelScope.launch {
            observeEvent().collect(::onEvent)
        }
    }

    fun sendEvent(event: Event) {
        viewModelScope.launch {
            eventBroadcast.emit(event)
        }
    }

    protected fun sendSideEffect(event: Event) {
        viewModelScope.launch {
            sideEffectBroadcast.emit(event)
        }
    }

    protected open fun onEvent(event: Event) {}

    open fun observeEvent(): Flow<Event> = eventBroadcast

    open fun observeSideEffects(): Flow<Event> = sideEffectBroadcast
}
