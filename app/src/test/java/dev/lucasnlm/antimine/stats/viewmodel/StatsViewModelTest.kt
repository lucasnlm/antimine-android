package dev.lucasnlm.antimine.stats.viewmodel

import dev.lucasnlm.antimine.IntentViewModelTest
import dev.lucasnlm.antimine.common.io.models.Stats
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.mocks.MemoryStatsRepository
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StatsViewModelTest : IntentViewModelTest() {
    private val listOfStats =
        listOf(
            // Standard
            Stats(1000, 10, 1, 6, 12, 30),
            Stats(1200, 11, 0, 6, 12, 20),
            // Expert
            Stats(2000, 99, 1, 24, 24, 90),
            Stats(3200, 99, 0, 24, 24, 20),
            // Intermediate
            Stats(4000, 40, 1, 16, 16, 40),
            Stats(5200, 40, 0, 16, 16, 10),
            // Beginner
            Stats(6000, 10, 1, 9, 9, 15),
            Stats(7200, 10, 0, 9, 9, 20),
            // Custom
            Stats(8000, 5, 1, 5, 5, 5),
            Stats(9200, 5, 0, 5, 5, 4),
        )

    private val prefsRepository: PreferencesRepository = mockk()
    private val minefieldRepository: MinefieldRepository = mockk()
    private val dimensionRepository: DimensionRepository = mockk()

    @Before
    override fun setup() {
        super.setup()
        every { prefsRepository.isPremiumEnabled() } returns false
        every { minefieldRepository.baseStandardSize(any(), any(), any()) } returns Minefield(6, 12, 9)
        every { minefieldRepository.fromDifficulty(any(), any(), any()) } returns Minefield(6, 12, 9)
    }

    @Test
    fun testStatsFileTotalGames() =
        runTest {
            val repository = MemoryStatsRepository(listOfStats.toMutableList())
            val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
            viewModel.sendEvent(StatsEvent.LoadStats)
            val statsFileModel = viewModel.singleState()
            assertEquals(10, statsFileModel.stats[0].totalGames)
            assertEquals(6, statsFileModel.stats[1].totalGames)
            assertEquals(8, statsFileModel.stats[2].totalGames)
        }

    @Test
    fun testStatsFileTotalGamesEmpty() =
        runTest {
            val repository = MemoryStatsRepository(mutableListOf())
            val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
            viewModel.sendEvent(StatsEvent.LoadStats)
            val statsFileModel = viewModel.singleState()
            assertEquals(0, statsFileModel.stats.size)
        }

    @Test
    fun testStatsFileMustGenerateStatsFileForEveryKindOfGamePlusGeneral() {
        val repository = MemoryStatsRepository(listOfStats.toMutableList())
        val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
        viewModel.sendEvent(StatsEvent.LoadStats)
        val statsFileModel = viewModel.singleState()

        // General, Standard, Beginner, Intermediate, Expert, Custom
        assertEquals(3, statsFileModel.stats.count())
    }

    @Test
    fun testStatsFileTotalTime() =
        runTest {
            val repository = MemoryStatsRepository(listOfStats.toMutableList())
            val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
            viewModel.sendEvent(StatsEvent.LoadStats)
            val statsFileModel = viewModel.singleState()

            assertEquals(47000, statsFileModel.stats[0].totalTime)
            assertEquals(16600, statsFileModel.stats[1].totalTime)
            assertEquals(44800, statsFileModel.stats[2].totalTime)
        }

    @Test
    fun testStatsFileAverageTime() =
        runTest {
            val repository = MemoryStatsRepository(listOfStats.toMutableList())
            val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
            viewModel.sendEvent(StatsEvent.LoadStats)
            val statsFileModel = viewModel.singleState()

            assertEquals(4200, statsFileModel.stats[0].averageTime)
            assertEquals(2333, statsFileModel.stats[1].averageTime)
            assertEquals(5000, statsFileModel.stats[2].averageTime)
        }

    @Test
    fun testStatsFileShortestTime() =
        runTest {
            val repository = MemoryStatsRepository(listOfStats.toMutableList())
            val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
            viewModel.sendEvent(StatsEvent.LoadStats)
            val statsFileModel = viewModel.singleState()

            assertEquals(1000, statsFileModel.stats[0].shortestTime)
            assertEquals(1000, statsFileModel.stats[1].shortestTime)
            assertEquals(2000, statsFileModel.stats[2].shortestTime)
        }

    @Test
    fun testStatsFileMines() =
        runTest {
            val repository = MemoryStatsRepository(listOfStats.toMutableList())
            val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
            viewModel.sendEvent(StatsEvent.LoadStats)

            val statsFileModel = viewModel.singleState()

            assertEquals(329, statsFileModel.stats[0].mines)
            assertEquals(299, statsFileModel.stats[1].mines)
            assertEquals(308, statsFileModel.stats[2].mines)
        }

    @Test
    fun testVictory() =
        runTest {
            val repository = MemoryStatsRepository(listOfStats.toMutableList())
            val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
            viewModel.sendEvent(StatsEvent.LoadStats)
            val statsFileModel = viewModel.singleState()

            assertEquals(5, statsFileModel.stats[0].victory)
            assertEquals(3, statsFileModel.stats[1].victory)
            assertEquals(4, statsFileModel.stats[2].victory)
        }

    @Test
    fun testOpenArea() =
        runTest {
            val repository = MemoryStatsRepository(listOfStats.toMutableList())
            val viewModel = StatsViewModel(repository, prefsRepository, minefieldRepository, dimensionRepository)
            viewModel.sendEvent(StatsEvent.LoadStats)
            val statsFileModel = viewModel.singleState()

            assertEquals(254, statsFileModel.stats[0].openArea)
            assertEquals(210, statsFileModel.stats[1].openArea)
            assertEquals(204, statsFileModel.stats[2].openArea)
        }
}
