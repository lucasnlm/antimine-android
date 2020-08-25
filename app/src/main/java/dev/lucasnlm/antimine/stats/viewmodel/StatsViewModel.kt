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
                StatsModel(statsCount, 0L, 0L, 0, 0, 0)
            ) { acc, value ->
                StatsModel(
                    acc.totalGames,
                    acc.duration + value.duration,
                    0,
                    acc.mines + value.mines,
                    acc.victory + value.victory,
                    acc.openArea + value.openArea
                )
            }
            result.copy(averageDuration = result.duration / result.totalGames)
        } else {
            emptyStats
        }
    }

    private suspend fun deleteAll() {
        statsRepository.getAllStats(0).lastOrNull()?.let {
            preferenceRepository.updateStatsBase(it.uid + 1)
        }
    }

    override fun initialState(): StatsModel = emptyStats

    override suspend fun mapEventToState(event: StatsEvent) = flow {
        when (event) {
            is StatsEvent.LoadStats -> {
                emit(loadStatsModel())
            }
            is StatsEvent.DeleteStats -> {
                deleteAll()
                emit(emptyStats)
            }
        }
    }

    companion object {
        val emptyStats = StatsModel(0, 0, 0, 0, 0, 0)
    }
}
