package dev.lucasnlm.antimine.stats.viewmodel

import dev.lucasnlm.antimine.IntentViewModelTest
import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.MemoryStatsRepository
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StatsViewModelTest : IntentViewModelTest() {

    private val listOfStats = listOf(
        // Standard
        Stats(0, 1000, 10, 1, 6, 12, 30),
        Stats(1, 1200, 11, 0, 6, 12, 20),

        // Expert
        Stats(2, 2000, 99, 1, 24, 24, 90),
        Stats(3, 3200, 99, 0, 24, 24, 20),

        // Intermediate
        Stats(4, 4000, 40, 1, 16, 16, 40),
        Stats(5, 5200, 40, 0, 16, 16, 10),

        // Beginner
        Stats(6, 6000, 10, 1, 9, 9, 15),
        Stats(7, 7200, 10, 0, 9, 9, 20),

        // Custom
        Stats(8, 8000, 5, 1, 5, 5, 5),
        Stats(9, 9200, 5, 0, 5, 5, 4),
    )

    private val prefsRepository: IPreferencesRepository = mockk()
    private val minefieldRepository: IMinefieldRepository = mockk()
    private val dimensionRepository: IDimensionRepository = mockk()

    @Before
    override fun setup() {
        super.setup()
        every { prefsRepository.getStatsBase() } returns 0
        every { prefsRepository.isPremiumEnabled() } returns false
        every { minefieldRepository.fromDifficulty(any(), any(), any()) } returns Minefield(6, 12, 9)
    }

    @Test
    fun testStatsTotalGames() = runBlockingTest {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
        viewModel.sendEvent(StatsEvent.LoadStats)
        val statsModel = viewModel.singleState()
        assertEquals(10, statsModel.stats[0].totalGames)
        assertEquals(2, statsModel.stats[1].totalGames)
        assertEquals(2, statsModel.stats[2].totalGames)
        assertEquals(2, statsModel.stats[3].totalGames)
        assertEquals(2, statsModel.stats[4].totalGames)
        assertEquals(2, statsModel.stats[5].totalGames)
    }

    @Test
    fun testStatsTotalGamesWithBase() = runBlockingTest {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)

        mapOf(0 to 6, 2 to 5, 4 to 4, 6 to 3, 8 to 2, 10 to 0).forEach { (base, expected) ->
            every { prefsRepository.getStatsBase() } returns base
            viewModel.sendEvent(StatsEvent.LoadStats)
            val statsModelBase0 = viewModel.singleState()
            assertEquals(expected, statsModelBase0.stats.size)
        }
    }

    @Test
    fun testStatsTotalGamesEmpty() = runBlockingTest {
        val repository = MemoryStatsRepository(mutableListOf())
        val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
        viewModel.sendEvent(StatsEvent.LoadStats)
        val statsModel = viewModel.singleState()
        assertEquals(0, statsModel.stats.size)
    }

    @Test
    fun testStatsMustGenerateStatsForEveryKindOfGamePlusGeneral() {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
        viewModel.sendEvent(StatsEvent.LoadStats)
        val statsModel = viewModel.singleState()

        // General, Standard, Beginner, Intermediate, Expert, Custom
        assertEquals(6, statsModel.stats.count())
    }

    @Test
    fun testStatsTotalTime() = runBlockingTest {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
        viewModel.sendEvent(StatsEvent.LoadStats)
        val statsModel = viewModel.singleState()

        assertEquals(47000, statsModel.stats[0].totalTime)
        assertEquals(2200, statsModel.stats[1].totalTime)
        assertEquals(5200, statsModel.stats[2].totalTime)
        assertEquals(9200, statsModel.stats[3].totalTime)
        assertEquals(13200, statsModel.stats[4].totalTime)
        assertEquals(17200, statsModel.stats[5].totalTime)
    }

    @Test
    fun testStatsAverageTime() = runBlockingTest {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
        viewModel.sendEvent(StatsEvent.LoadStats)
        val statsModel = viewModel.singleState()

        assertEquals(4200, statsModel.stats[0].averageTime)
        assertEquals(1000, statsModel.stats[1].averageTime)
        assertEquals(2000, statsModel.stats[2].averageTime)
        assertEquals(4000, statsModel.stats[3].averageTime)
        assertEquals(6000, statsModel.stats[4].averageTime)
        assertEquals(8000, statsModel.stats[5].averageTime)
    }

    @Test
    fun testStatsShortestTime() = runBlockingTest {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
        viewModel.sendEvent(StatsEvent.LoadStats)
        val statsModel = viewModel.singleState()

        assertEquals(1000, statsModel.stats[0].shortestTime)
        assertEquals(1000, statsModel.stats[1].shortestTime)
        assertEquals(2000, statsModel.stats[2].shortestTime)
        assertEquals(4000, statsModel.stats[3].shortestTime)
        assertEquals(6000, statsModel.stats[4].shortestTime)
        assertEquals(8000, statsModel.stats[5].shortestTime)
    }

    @Test
    fun testStatsMines() = runBlockingTest {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
        viewModel.sendEvent(StatsEvent.LoadStats)
        val statsModel = viewModel.singleState()

        assertEquals(329, statsModel.stats[0].mines)
        assertEquals(21, statsModel.stats[1].mines)
        assertEquals(198, statsModel.stats[2].mines)
        assertEquals(80, statsModel.stats[3].mines)
        assertEquals(20, statsModel.stats[4].mines)
        assertEquals(10, statsModel.stats[5].mines)
    }

    @Test
    fun testVictory() = runBlockingTest {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
        viewModel.sendEvent(StatsEvent.LoadStats)
        val statsModel = viewModel.singleState()

        assertEquals(5, statsModel.stats[0].victory)
        assertEquals(1, statsModel.stats[1].victory)
        assertEquals(1, statsModel.stats[2].victory)
        assertEquals(1, statsModel.stats[3].victory)
        assertEquals(1, statsModel.stats[4].victory)
        assertEquals(1, statsModel.stats[5].victory)
    }

    @Test
    fun testOpenArea() = runBlockingTest {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
        viewModel.sendEvent(StatsEvent.LoadStats)
        val statsModel = viewModel.singleState()

        assertEquals(254, statsModel.stats[0].openArea)
        assertEquals(50, statsModel.stats[1].openArea)
        assertEquals(110, statsModel.stats[2].openArea)
        assertEquals(50, statsModel.stats[3].openArea)
        assertEquals(35, statsModel.stats[4].openArea)
        assertEquals(35, statsModel.stats[4].openArea)
    }
}
