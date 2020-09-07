package dev.lucasnlm.antimine.stats.viewmodel

import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.stats.model.StatsModel
import kotlinx.coroutines.flow.flow

class StatsViewModel(
    private val statsRepository: IStatsRepository,
    private val preferenceRepository: IPreferencesRepository,
) : IntentViewModel<StatsEvent, StatsModel>() {
    private suspend fun loadStatsModel(): StatsModel {
        val minId = preferenceRepository.getStatsBase()
        val stats = statsRepository.getAllStats(minId)
        val statsCount = stats.count()

        return if (statsCount > 0) {
            val result = stats.fold(
                StatsModel(
                    totalGames = statsCount,
                    duration = 0,
                    averageDuration = 0,
                    mines = 0,
                    victory = 0,
                    openArea = 0,
                    showAds = !preferenceRepository.isPremiumEnabled(),
                )
            ) { acc, value ->
                StatsModel(
                    acc.totalGames,
                    acc.duration + value.duration,
                    0,
                    acc.mines + value.mines,
                    acc.victory + value.victory,
                    acc.openArea + value.openArea,
                    showAds = !preferenceRepository.isPremiumEnabled(),
                )
            }
            result.copy(averageDuration = result.duration / result.totalGames)
        } else {
            StatsModel(
                totalGames = 0,
                duration = 0,
                averageDuration = 0,
                mines = 0,
                victory = 0,
                openArea = 0,
                showAds = !preferenceRepository.isPremiumEnabled()
            )
        }
    }

    private suspend fun deleteAll() {
        statsRepository.getAllStats(0).lastOrNull()?.let {
            preferenceRepository.updateStatsBase(it.uid + 1)
        }
    }

    override fun initialState() = StatsModel(
        totalGames = 0,
        duration = 0,
        averageDuration = 0,
        mines = 0,
        victory = 0,
        openArea = 0,
        showAds = !preferenceRepository.isPremiumEnabled()
    )

    override suspend fun mapEventToState(event: StatsEvent) = flow {
        when (event) {
            is StatsEvent.LoadStats -> {
                emit(loadStatsModel())
            }
            is StatsEvent.DeleteStats -> {
                deleteAll()
                emit(
                    state.copy(
                        totalGames = 0,
                        duration = 0,
                        averageDuration = 0,
                        mines = 0,
                        victory = 0,
                        openArea = 0,
                    )
                )
            }
        }
    }
}
