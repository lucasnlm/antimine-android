package dev.lucasnlm.antimine.common.level.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackInteractor
import dev.lucasnlm.antimine.core.analytics.AnalyticsManager
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import javax.inject.Inject

class GameViewModelFactory @Inject constructor(
    private val application: Application,
    private val eventObserver: MutableLiveData<Event>,
    private val savesRepository: ISavesRepository,
    private val dimensionRepository: IDimensionRepository,
    private val preferencesRepository: IPreferencesRepository,
    private val hapticFeedbackInteractor: IHapticFeedbackInteractor,
    private val minefieldRepository: MinefieldRepository,
    private val analyticsManager: AnalyticsManager,
    private val clock: Clock
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            GameViewModel(
                application,
                eventObserver,
                savesRepository,
                dimensionRepository,
                preferencesRepository,
                hapticFeedbackInteractor,
                minefieldRepository,
                analyticsManager,
                clock
            ) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
}
