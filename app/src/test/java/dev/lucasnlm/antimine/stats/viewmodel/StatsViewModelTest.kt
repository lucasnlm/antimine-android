package dev.lucasnlm.antimine.stats.viewmodel

import dev.lucasnlm.antimine.IntentViewModelTest
import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.mocks.MemoryStatsRepository
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest : IntentViewModelTest() {

    private val listOfStats =
        listOf(
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

    private val prefsRepository: PreferencesRepository = mockk()
    private val minefieldRepository: MinefieldRepository = mockk()
    private val dimensionRepository: DimensionRepository = mockk()

    @Before
    override fun setup() {
        super.setup()
        every { prefsRepository.getStatsBase() } returns 0
        every { prefsRepository.isPremiumEnabled() } returns false
        every { minefieldRepository.baseStandardSize(any(), any()) } returns Minefield(6, 12, 9)
        every { minefieldRepository.fromDifficulty(any(), any(), any()) } returns Minefield(6, 12, 9)
    }

    @Test
    fun testStatsTotalGames() =
        runTest {
            val repository = MemoryStatsRepository(listOfStats.toMutableList())
            val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
            viewModel.sendEvent(StatsEvent.LoadStats)
            val statsModel = viewModel.singleState()
            assertEquals(10, statsModel.stats[0].totalGames)
            assertEquals(6, statsModel.stats[1].totalGames)
            assertEquals(8, statsModel.stats[2].totalGames)
        }

    @Test
    fun testStatsTotalGamesWithBase() =
        runTest {
            val repository = MemoryStatsRepository(listOfStats.toMutableList())
            val viewModel =
                StatsViewModel(
                    statsRepository = repository,
                    preferenceRepository = prefsRepository,
                    minefieldRepository = minefieldRepository,
                    dimensionRepository = dimensionRepository,
                )

            mapOf(
                0 to 3,
                2 to 3,
                4 to 3,
                6 to 2,
                8 to 2,
                10 to 0,
            ).forEach { (base, expected) ->
                every { prefsRepository.getStatsBase() } returns base
                viewModel.sendEvent(StatsEvent.LoadStats)
                val statsModelBase = viewModel.singleState()
                val current = statsModelBase.stats.size
                assertEquals(expected, current)
            }
        }

    @Test
    fun testStatsTotalGamesEmpty() =
        runTest {
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
        assertEquals(3, statsModel.stats.count())
    }

    @Test
    fun testStatsTotalTime() =
        runTest {
            val repository = MemoryStatsRepository(listOfStats.toMutableList())
            val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
            viewModel.sendEvent(StatsEvent.LoadStats)
            val statsModel = viewModel.singleState()

            assertEquals(47000, statsModel.stats[0].totalTime)
            assertEquals(16600, statsModel.stats[1].totalTime)
            assertEquals(44800, statsModel.stats[2].totalTime)
        }

    @Test
    fun testStatsAverageTime() =
        runTest {
            val repository = MemoryStatsRepository(listOfStats.toMutableList())
            val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
            viewModel.sendEvent(StatsEvent.LoadStats)
            val statsModel = viewModel.singleState()

            assertEquals(4200, statsModel.stats[0].averageTime)
            assertEquals(2333, statsModel.stats[1].averageTime)
            assertEquals(5000, statsModel.stats[2].averageTime)
        }

    @Test
    fun testStatsShortestTime() =
        runTest {
            val repository = MemoryStatsRepository(listOfStats.toMutableList())
            val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
            viewModel.sendEvent(StatsEvent.LoadStats)
            val statsModel = viewModel.singleState()

            assertEquals(1000, statsModel.stats[0].shortestTime)
            assertEquals(1000, statsModel.stats[1].shortestTime)
            assertEquals(2000, statsModel.stats[2].shortestTime)
        }

    @Test
    fun testStatsMines() =
        runTest {
            val repository = MemoryStatsRepository(listOfStats.toMutableList())
            val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
            viewModel.sendEvent(StatsEvent.LoadStats)
            val statsModel = viewModel.singleState()

            assertEquals(329, statsModel.stats[0].mines)
            assertEquals(299, statsModel.stats[1].mines)
            assertEquals(308, statsModel.stats[2].mines)
        }

    @Test
    fun testVictory() =
        runTest {
            val repository = MemoryStatsRepository(listOfStats.toMutableList())
            val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
            viewModel.sendEvent(StatsEvent.LoadStats)
            val statsModel = viewModel.singleState()

            assertEquals(5, statsModel.stats[0].victory)
            assertEquals(3, statsModel.stats[1].victory)
            assertEquals(4, statsModel.stats[2].victory)
        }

    @Test
    fun testOpenArea() =
        runTest {
            val repository = MemoryStatsRepository(listOfStats.toMutableList())
            val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
            viewModel.sendEvent(StatsEvent.LoadStats)
            val statsModel = viewModel.singleState()

            assertEquals(254, statsModel.stats[0].openArea)
            assertEquals(210, statsModel.stats[1].openArea)
            assertEquals(204, statsModel.stats[2].openArea)
        }
}
