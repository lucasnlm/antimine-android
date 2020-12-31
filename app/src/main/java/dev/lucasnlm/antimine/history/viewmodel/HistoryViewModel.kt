package dev.lucasnlm.antimine.history.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.lucasnlm.antimine.DeepLink
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import kotlinx.coroutines.flow.flow

class HistoryViewModel(
    private val context: Context,
    private val savesRepository: ISavesRepository,
    private val preferencesRepository: IPreferencesRepository,
) : IntentViewModel<HistoryEvent, HistoryState>() {

    override fun initialState() = HistoryState(
        saveList = listOf(),
        showAds = !preferencesRepository.isPremiumEnabled(),
    )

    override fun onEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.LoadSave -> {
                loadGame(event.id)
            }
            is HistoryEvent.ReplaySave -> {
                replayGame(event.id)
            }
            else -> { }
        }
    }

    override suspend fun mapEventToState(event: HistoryEvent) = flow {
        when (event) {
            is HistoryEvent.LoadAllSaves -> {
                val newSaveList = savesRepository.getAllSaves().sortedByDescending { it.uid }
                emit(state.copy(saveList = newSaveList))
            }
            else -> { }
        }
    }

    private fun replayGame(uid: Int) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.Builder()
                .scheme(DeepLink.SCHEME)
                .authority(DeepLink.RETRY_HOST_AUTHORITY)
                .appendPath(uid.toString())
                .build()
        }
        context.startActivity(intent)
    }

    private fun loadGame(uid: Int) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.Builder()
                .scheme(DeepLink.SCHEME)
                .authority(DeepLink.LOAD_GAME_AUTHORITY)
                .appendPath(uid.toString())
                .build()
        }
        context.startActivity(intent)
    }
}
