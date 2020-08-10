package dev.lucasnlm.antimine.core.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

open class StatelessViewModel<Event> : ViewModel() {
    private val eventBroadcast = ConflatedBroadcastChannel<Event>()

    fun sendEvent(event: Event) {
        eventBroadcast.offer(event)
    }

    override fun onCleared() {
        super.onCleared()
        eventBroadcast.close()
    }

    fun observeEvent(): Flow<Event> = eventBroadcast.asFlow()
}
