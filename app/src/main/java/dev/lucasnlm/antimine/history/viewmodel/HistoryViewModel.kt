package dev.lucasnlm.antimine.history.viewmodel

import android.content.Context
import android.content.Intent
import dev.lucasnlm.antimine.GameActivity
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import kotlinx.coroutines.flow.flow

class HistoryViewModel(
    private val context: Context,
    private val savesRepository: ISavesRepository,
) : IntentViewModel<HistoryEvent, HistoryState>() {

    override fun initialState() = HistoryState(
        saveList = listOf(),
    )

    override fun onEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.LoadSave -> {
                loadGame(event.id)
            }
            is HistoryEvent.ReplaySave -> {
                replayGame(event.id)
            }
            else -> {
            }
        }
    }

    override suspend fun mapEventToState(event: HistoryEvent) = flow {
        when (event) {
            is HistoryEvent.LoadAllSaves -> {
                val newSaveList = savesRepository.getAllSaves().sortedByDescending { it.uid }
                emit(state.copy(saveList = newSaveList))
            }
            else -> {
            }
        }
    }

    private fun replayGame(uid: Int) {
        val intent = Intent(context, GameActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(GameActivity.RETRY_GAME, uid)
        }
        context.startActivity(intent)
    }

    private fun loadGame(uid: Int) {
        val intent = Intent(context, GameActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(GameActivity.START_GAME, uid)
        }
        context.startActivity(intent)
    }
}
