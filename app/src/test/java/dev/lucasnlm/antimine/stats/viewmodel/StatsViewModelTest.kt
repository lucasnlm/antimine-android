package dev.lucasnlm.antimine.stats.viewmodel

import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.common.level.repository.MemoryStatsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

@ExperimentalCoroutinesApi
class StatsViewModelTest {
    private val listOfStats = listOf(
        Stats(0, 1000, 10, 1, 10, 10, 90),
        Stats(1, 1200, 24, 0, 10, 10, 20)
    )

    @Test
    fun testStatsTotalGames() = runBlockingTest {
        val viewModel = StatsViewModel()
        val statsModel = viewModel.getStatsModel(MemoryStatsRepository(listOfStats.toMutableList()))
        assertEquals(2, statsModel?.totalGames)

        val emptyStatsModel = viewModel.getStatsModel(MemoryStatsRepository())
        assertEquals(0, emptyStatsModel?.totalGames)
    }

    @Test
    fun testStatsDuration() = runBlockingTest {
        val viewModel = StatsViewModel()
        val statsModel = viewModel.getStatsModel(MemoryStatsRepository(listOfStats.toMutableList()))
        assertEquals(2200L, statsModel?.duration)

        val emptyStatsModel = viewModel.getStatsModel(MemoryStatsRepository())
        assertEquals(0L, emptyStatsModel?.duration)
    }

    @Test
    fun testStatsAverageDuration() = runBlockingTest {
        val viewModel = StatsViewModel()
        val statsModel = viewModel.getStatsModel(MemoryStatsRepository(listOfStats.toMutableList()))
        assertEquals(1100L, statsModel?.averageDuration)

        val emptyStatsModel = viewModel.getStatsModel(MemoryStatsRepository(mutableListOf()))
        assertEquals(0L, emptyStatsModel?.averageDuration)
    }

    @Test
    fun testStatsMines() = runBlockingTest {
        val viewModel = StatsViewModel()
        val statsModel = viewModel.getStatsModel(MemoryStatsRepository(listOfStats.toMutableList()))
        assertEquals(34, statsModel?.mines)

        val emptyStatsModel = viewModel.getStatsModel(MemoryStatsRepository(mutableListOf()))
        assertEquals(0, emptyStatsModel?.mines)
    }

    @Test
    fun testVictory() = runBlockingTest {
        val viewModel = StatsViewModel()
        val statsModel = viewModel.getStatsModel(MemoryStatsRepository(listOfStats.toMutableList()))
        assertEquals(1, statsModel?.victory)

        val emptyStatsModel = viewModel.getStatsModel(MemoryStatsRepository(mutableListOf()))
        assertEquals(0, emptyStatsModel?.victory)
    }

    @Test
    fun testOpenArea() = runBlockingTest {
        val viewModel = StatsViewModel()
        val statsModel = viewModel.getStatsModel(MemoryStatsRepository(listOfStats.toMutableList()))
        assertEquals(110, statsModel?.openArea)

        val emptyStatsModel = viewModel.getStatsModel(MemoryStatsRepository(mutableListOf()))
        assertEquals(0, emptyStatsModel?.openArea)
    }
}
