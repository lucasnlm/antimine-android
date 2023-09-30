package dev.lucasnlm.antimine.history.viewmodel

import android.app.Application
import android.content.Intent
import dev.lucasnlm.antimine.GameActivity
import dev.lucasnlm.antimine.common.level.repository.SavesRepository
import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import kotlinx.coroutines.flow.flow

class HistoryViewModel(
    private val application: Application,
    private val savesRepository: SavesRepository,
    private val audioManager: GameAudioManager,
) : IntentViewModel<HistoryEvent, HistoryState>() {

    override fun initialState() =
        HistoryState(
            saveList = listOf(),
            loading = true,
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

    override suspend fun mapEventToState(event: HistoryEvent) =
        flow {
            when (event) {
                is HistoryEvent.LoadAllSaves -> {
                    emit(state.copy(loading = true))
                    val newSaveList = savesRepository.getAllSaves().sortedByDescending { it.startDate }
                    emit(
                        state.copy(
                            saveList = newSaveList,
                            loading = false,
                        ),
                    )
                }
                else -> {
                }
            }
        }

    private fun replayGame(saveId: String) {
        audioManager.playClickSound()

        val context = application.applicationContext
        val intent =
            Intent(context, GameActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(GameActivity.RETRY_GAME, saveId)
            }
        context.startActivity(intent)
    }

    private fun loadGame(saveId: String) {
        audioManager.playClickSound()

        val context = application.applicationContext
        val intent =
            Intent(context, GameActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(GameActivity.START_GAME, saveId)
            }
        context.startActivity(intent)
    }
}
