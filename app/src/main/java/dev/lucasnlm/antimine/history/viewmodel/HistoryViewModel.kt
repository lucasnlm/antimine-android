package dev.lucasnlm.antimine.history.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository

class HistoryViewModel : ViewModel() {
    val saves = MutableLiveData<List<Save>>()

    suspend fun loadAllSaves(saveDao: ISavesRepository) {
        saves.postValue(saveDao.getAllSaves().sortedByDescending { it.uid })
    }
}
