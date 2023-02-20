package dev.lucasnlm.antimine.core.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class IntentViewModel<Event, State> : StatelessViewModel<Event>() {
    private val mutableState: MutableStateFlow<State> by lazy { MutableStateFlow(initialState()) }

    protected val state: State
        get() = mutableState.value

    init {
        viewModelScope.launch {
            @Suppress("OPT_IN_USAGE")
            observeEvent()
                .onEach { onEvent(it) }
                .flatMapConcat(::mapEventToState)
                .distinctUntilChanged()
                .collect { mutableState.value = it }
        }
    }

    protected abstract fun initialState(): State

    protected open suspend fun mapEventToState(event: Event): Flow<State> = flow { }

    fun observeState(): StateFlow<State> = mutableState

    fun singleState(): State = state
}
