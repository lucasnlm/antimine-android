package dev.lucasnlm.antimine.core.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class IntentViewModel<Event, State> : StatelessViewModel<Event>() {
    private val mutableState: MutableStateFlow<State> by lazy { MutableStateFlow(initialState()) }

    protected val state: State
        get() = mutableState.value

    init {
        viewModelScope.launch {
            observeEvent()
                .onEach { handleEvent(it) }
                .flatMapConcat(::mapEventToState)
                .distinctUntilChanged()
                .collect { mutableState.value = it }
        }
    }

    protected abstract fun initialState(): State

    protected open suspend fun mapEventToState(event: Event): Flow<State> = flow { }

    protected open suspend fun handleEvent(event: Event) {}

    fun observeState(): StateFlow<State> = mutableState

    fun singleState(): State = state
}
