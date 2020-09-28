package dev.lucasnlm.antimine.stats.viewmodel

import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.stats.model.StatsModel
import dev.lucasnlm.antimine.stats.model.StatsState
import kotlinx.coroutines.flow.flow

class StatsViewModel(
    private val statsRepository: IStatsRepository,
    private val preferenceRepository: IPreferencesRepository,
) : IntentViewModel<StatsEvent, StatsState>() {
    private suspend fun loadStatsModel(): List<StatsModel> {
        val minId = preferenceRepository.getStatsBase()
        val stats = statsRepository.getAllStats(minId)

        return listOf(
            // General
            stats.fold().copy(title = R.string.general),

            // Expert
            stats.filter {
                it.mines == 99 && it.width == 24 && it.height == 24
            }.fold().copy(title = R.string.expert),

            // Intermediate
            stats.filter {
                it.mines == 40 && it.width == 16 && it.height == 16
            }.fold().copy(title = R.string.intermediate),

            // Beginner
            stats.filter {
                it.mines == 10 && it.width == 9 && it.height == 9
            }.fold().copy(title = R.string.beginner),

            // Custom
            stats.filterNot {
                it.mines == 99 && it.width == 24 && it.height == 24
            }.filterNot {
                it.mines == 40 && it.width == 16 && it.height == 16
            }.filterNot {
                it.mines == 10 && it.width == 9 && it.height == 9
            }.fold().copy(title = R.string.custom),
        ).filter {
            it.totalGames > 0
        }
    }

    private suspend fun deleteAll() {
        statsRepository.getAllStats(0).lastOrNull()?.let {
            preferenceRepository.updateStatsBase(it.uid + 1)
        }
    }

    private fun List<Stats>.fold(): StatsModel {
        return if (size > 0) {
            val result = fold(
                StatsModel(
                    title = 0,
                    totalGames = size,
                    duration = 0,
                    averageDuration = 0,
                    mines = 0,
                    victory = 0,
                    openArea = 0,
                )
            ) { acc, value ->
                StatsModel(
                    0,
                    acc.totalGames,
                    acc.duration + value.duration,
                    0,
                    acc.mines + value.mines,
                    acc.victory + value.victory,
                    acc.openArea + value.openArea,
                )
            }
            result.copy(averageDuration = result.duration / result.totalGames)
        } else {
            StatsModel(
                title = 0,
                totalGames = 0,
                duration = 0,
                averageDuration = 0,
                mines = 0,
                victory = 0,
                openArea = 0,
            )
        }
    }

    override fun initialState() = StatsState(
        stats = listOf(),
        showAds = !preferenceRepository.isPremiumEnabled()
    )

    override suspend fun mapEventToState(event: StatsEvent) = flow {
        when (event) {
            is StatsEvent.LoadStats -> {
                emit(state.copy(stats = loadStatsModel()))
            }
            is StatsEvent.DeleteStats -> {
                deleteAll()
                emit(state.copy(stats = loadStatsModel()))
            }
        }
    }
}
