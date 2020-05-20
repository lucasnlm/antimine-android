package dev.lucasnlm.antimine.stats.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.stats.model.StatsModel

class StatsViewModel : ViewModel() {
    val statsObserver = MutableLiveData<StatsModel>()

    suspend fun getStatsModel(statsRepository: IStatsRepository): StatsModel? {
        val stats = statsRepository.getAllStats()
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
            StatsModel(0, 0, 0, 0, 0, 0)
        }
    }

    suspend fun loadStats(statsRepository: IStatsRepository) {
        getStatsModel(statsRepository)?.let {
            if (it.totalGames > 0) {
                statsObserver.postValue(it)
            }
        }
    }
}
