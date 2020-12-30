package dev.lucasnlm.antimine.stats.viewmodel

import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.stats.model.StatsModel
import dev.lucasnlm.antimine.stats.model.StatsState
import kotlinx.coroutines.flow.flow

class StatsViewModel(
    private val statsRepository: IStatsRepository,
    private val preferenceRepository: IPreferencesRepository,
    private val minefieldRepository: IMinefieldRepository,
    private val dimensionRepository: IDimensionRepository,
) : IntentViewModel<StatsEvent, StatsState>() {
    private suspend fun loadStatsModel(): List<StatsModel> {
        val minId = preferenceRepository.getStatsBase()
        val stats = statsRepository.getAllStats(minId)
        val standardSize = minefieldRepository.fromDifficulty(
            Difficulty.Standard,
            dimensionRepository,
            preferenceRepository,
        )

        return listOf(
            // General
            stats.fold().copy(title = R.string.general),

            // Standard
            stats.filter {
                it.width == standardSize.width && it.height == standardSize.height
            }.fold().copy(title = R.string.standard),

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
            }.filterNot {
                it.width == standardSize.width && it.height == standardSize.height
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
            fold(
                StatsModel(
                    title = 0,
                    totalGames = size,
                    totalTime = 0,
                    victoryTime = 0,
                    averageTime = 0,
                    shortestTime = 0,
                    mines = 0,
                    victory = 0,
                    openArea = 0,
                )
            ) { acc, value ->
                StatsModel(
                    0,
                    acc.totalGames,
                    acc.totalTime + value.duration,
                    victoryTime = acc.victoryTime + if (value.victory != 0) { value.duration } else { 0 },
                    averageTime = 0,
                    shortestTime = if (value.victory != 0) {
                        if (acc.shortestTime == 0L) {
                            value.duration
                        } else {
                            acc.shortestTime.coerceAtMost(value.duration)
                        }
                    } else {
                        acc.shortestTime
                    },
                    acc.mines + value.mines,
                    acc.victory + value.victory,
                    acc.openArea + value.openArea,
                )
            }.run {
                if (victory > 0) {
                    copy(averageTime = victoryTime / victory)
                } else {
                    this
                }
            }
        } else {
            StatsModel(
                title = 0,
                totalGames = 0,
                totalTime = 0,
                victoryTime = 0,
                averageTime = 0,
                shortestTime = 0,
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
