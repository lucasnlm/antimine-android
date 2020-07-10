package dev.lucasnlm.antimine.stats.viewmodel

import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.common.level.repository.MemoryStatsRepository
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class StatsViewModelTest {
    private val listOfStats = listOf(
        Stats(0, 1000, 10, 1, 10, 10, 90),
        Stats(1, 1200, 24, 0, 10, 10, 20)
    )

    private val prefsRepository: IPreferencesRepository = mockk()

    @Before
    fun setup() {
        every { prefsRepository.getStatsBase() } returns 0
    }

    @Test
    fun testStatsTotalGames() = runBlockingTest {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository)
        val statsModel = viewModel.getStatsModel()
        assertEquals(2, statsModel?.totalGames)
    }

    @Test
    fun testStatsTotalGamesWithBase() = runBlockingTest {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository)

        every { prefsRepository.getStatsBase() } returns 0
        val statsModelBase0 = viewModel.getStatsModel()
        assertEquals(2, statsModelBase0?.totalGames)

        every { prefsRepository.getStatsBase() } returns 1
        val statsModelBase1 = viewModel.getStatsModel()
        assertEquals(1, statsModelBase1?.totalGames)

        every { prefsRepository.getStatsBase() } returns 2
        val statsModelBase2 = viewModel.getStatsModel()
        assertEquals(0, statsModelBase2?.totalGames)
    }

    @Test
    fun testStatsTotalGamesEmpty() = runBlockingTest {
        val repository = MemoryStatsRepository(mutableListOf())
        val viewModel = StatsViewModel(repository, prefsRepository)
        val statsModel = viewModel.getStatsModel()
        assertEquals(0, statsModel?.totalGames)
    }

    @Test
    fun testStatsDuration() = runBlockingTest {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository)
        val statsModel = viewModel.getStatsModel()

        assertEquals(2200L, statsModel?.duration)
    }

    @Test
    fun testStatsDurationEmpty() = runBlockingTest {
        val repository = MemoryStatsRepository(mutableListOf())
        val viewModel = StatsViewModel(repository, prefsRepository)
        val statsModel = viewModel.getStatsModel()

        assertEquals(0L, statsModel?.duration)
    }

    @Test
    fun testStatsAverageDuration() = runBlockingTest {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository)
        val statsModel = viewModel.getStatsModel()

        assertEquals(1100L, statsModel?.averageDuration)
    }

    @Test
    fun testStatsAverageDurationEmpty() = runBlockingTest {
        val repository = MemoryStatsRepository(mutableListOf())
        val viewModel = StatsViewModel(repository, prefsRepository)
        val statsModel = viewModel.getStatsModel()

        assertEquals(0L, statsModel?.averageDuration)
    }

    @Test
    fun testStatsMines() = runBlockingTest {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository)
        val statsModel = viewModel.getStatsModel()
        assertEquals(34, statsModel?.mines)
    }

    @Test
    fun testStatsMinesEmpty() = runBlockingTest {
        val repository = MemoryStatsRepository(mutableListOf())
        val viewModel = StatsViewModel(repository, prefsRepository)
        val statsModel = viewModel.getStatsModel()
        assertEquals(0, statsModel?.mines)
    }

    @Test
    fun testVictory() = runBlockingTest {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository)
        val statsModel = viewModel.getStatsModel()

        assertEquals(1, statsModel?.victory)
    }

    @Test
    fun testVictoryEmpty() = runBlockingTest {
        val repository = MemoryStatsRepository(mutableListOf())
        val viewModel = StatsViewModel(repository, prefsRepository)
        val statsModel = viewModel.getStatsModel()

        assertEquals(0, statsModel?.victory)
    }

    @Test
    fun testOpenArea() = runBlockingTest {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository)
        val statsModel = viewModel.getStatsModel()

        assertEquals(110, statsModel?.openArea)
    }

    @Test
    fun testOpenAreaEmpty() = runBlockingTest {
        val repository = MemoryStatsRepository(mutableListOf())
        val viewModel = StatsViewModel(repository, prefsRepository)
        val statsModel = viewModel.getStatsModel()

        assertEquals(0, statsModel?.openArea)
    }
}
