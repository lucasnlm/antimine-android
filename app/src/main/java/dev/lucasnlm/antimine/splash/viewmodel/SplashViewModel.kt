package dev.lucasnlm.antimine.splash.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.support.IapHandler
import dev.lucasnlm.external.ICloudStorageManager
import dev.lucasnlm.external.model.CloudSave
import kotlinx.coroutines.launch

class SplashViewModel(
    private val preferencesRepository: IPreferencesRepository,
    private val statsRepository: IStatsRepository,
    private val saveCloudStorageManager: ICloudStorageManager,
    private val iapHandler: IapHandler,
) : ViewModel() {
    fun startIap() {
        viewModelScope.launch {
            iapHandler.start()
        }
    }

    companion object {
        val TAG = SplashViewModel::class.simpleName
    }
}
