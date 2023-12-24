package dev.lucasnlm.antimine.stats.viewmodel

import dev.lucasnlm.antimine.common.io.models.Stats
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.StatsRepository
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.stats.model.StatsModel
import dev.lucasnlm.antimine.stats.model.StatsState
import kotlinx.coroutines.flow.flow
import dev.lucasnlm.antimine.i18n.R as i18n

class StatsViewModel(
    private val statsRepository: StatsRepository,
    private val preferenceRepository: PreferencesRepository,
    private val minefieldRepository: MinefieldRepository,
    private val dimensionRepository: DimensionRepository,
) : IntentViewModel<StatsEvent, StatsState>() {

    private val legendSize = sizeOf(Difficulty.Legend)
    private val masterSize = sizeOf(Difficulty.Master)
    private val expertSize = sizeOf(Difficulty.Expert)
    private val intermediateSize = sizeOf(Difficulty.Intermediate)
    private val beginnerSize = sizeOf(Difficulty.Beginner)
    private val standardSize =
        minefieldRepository.baseStandardSize(
            dimensionRepository = dimensionRepository,
            progressiveMines = 0,
            limitToMax = false,
        )

    private fun sizeOf(difficulty: Difficulty): Minefield {
        return minefieldRepository.fromDifficulty(
            difficulty,
            dimensionRepository,
            preferenceRepository,
        )
    }

    private suspend fun loadStatsModel(): List<StatsModel> {
        val stats = statsRepository.getAllStats()

        return with(stats) {
            listOf(
                // General
                fold().copy(title = i18n.string.general),
                // Progressive
                filterStandard(standardSize).fold().copy(title = i18n.string.progressive),
                // Fixed Size
                filter(::isFixedSize).fold().copy(title = i18n.string.fixed_size),
                // Legend
                filter(::isLegend).fold().copy(title = i18n.string.legend),
                // Master
                filter(::isMaster).fold().copy(title = i18n.string.master),
                // Expert
                filter(::isExpert).fold().copy(title = i18n.string.expert),
                // Intermediate
                filter(::isIntermediate).fold().copy(title = i18n.string.intermediate),
                // Beginner
                filter(::isBeginner).fold().copy(title = i18n.string.beginner),
                // Custom
                asSequence()
                    .filterNot(::isExpert)
                    .filterNot(::isIntermediate)
                    .filterNot(::isBeginner)
                    .filterNot(::isMaster)
                    .filterNot(::isLegend)
                    .filterNot(::isFixedSize)
                    .filterNotStandard(standardSize)
                    .toList()
                    .fold()
                    .copy(title = i18n.string.custom),
            ).filter {
                it.totalGames > 0
            }
        }
    }

    private suspend fun deleteAll() {
        statsRepository.deleteLastStats()
    }

    private fun List<Stats>.fold(): StatsModel {
        return if (isNotEmpty()) {
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
                ),
            ) { acc, value ->
                StatsModel(
                    title = 0,
                    totalGames = acc.totalGames,
                    totalTime = acc.totalTime + value.duration,
                    victoryTime =
                        acc.victoryTime +
                            if (value.victory != 0) {
                                value.duration
                            } else {
                                0
                            },
                    averageTime = 0,
                    shortestTime =
                        if (value.victory != 0) {
                            if (acc.shortestTime == 0L) {
                                value.duration
                            } else {
                                acc.shortestTime.coerceAtMost(value.duration)
                            }
                        } else {
                            acc.shortestTime
                        },
                    mines = acc.mines + value.mines,
                    victory = acc.victory + value.victory,
                    openArea = acc.openArea + value.openArea,
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

    override fun initialState() =
        StatsState(
            stats = listOf(),
        )

    override suspend fun mapEventToState(event: StatsEvent) =
        flow {
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

    private fun Stats.isSizeOf(minefield: Minefield): Boolean {
        return this.mines == minefield.mines && this.width == minefield.width && this.height == minefield.height
    }

    private fun isExpert(stats: Stats): Boolean {
        return stats.isSizeOf(expertSize)
    }

    private fun isMaster(stats: Stats): Boolean {
        return stats.isSizeOf(masterSize)
    }

    private fun isLegend(stats: Stats): Boolean {
        return stats.isSizeOf(legendSize)
    }

    private fun isFixedSize(stats: Stats): Boolean {
        return stats.isSizeOf(standardSize)
    }

    private fun isIntermediate(stats: Stats): Boolean {
        return stats.isSizeOf(intermediateSize)
    }

    private fun isBeginner(stats: Stats): Boolean {
        return stats.isSizeOf(beginnerSize)
    }

    private fun List<Stats>.filterStandard(standardSize: Minefield) =
        filter {
            val baseWidth = (it.width - standardSize.width)
            val baseHeight = (it.height - standardSize.height)
            val baseWidthInv = (it.height - standardSize.width)
            val baseHeightInv = (it.width - standardSize.height)

            val baseCheck = (baseWidth >= 0 && baseWidth % 2 == 0 && baseHeight >= 0 && baseHeight % 2 == 0)
            val baseInvCheck =
                (baseWidthInv >= 0 && baseWidthInv % 2 == 0 && baseHeightInv >= 0 && baseHeightInv % 2 == 0)

            (baseCheck || baseInvCheck) &&
                listOf(::isExpert, ::isMaster, ::isLegend, ::isIntermediate, ::isBeginner)
                    .any { func -> func.invoke(it) }
                    .not()
        }

    private fun Sequence<Stats>.filterNotStandard(standardSize: Minefield) =
        filterNot {
            (it.width == standardSize.width && it.height == standardSize.height) ||
                (it.width == standardSize.height && it.height == standardSize.width)
        }
}
