package dev.lucasnlm.antimine.history

import dev.lucasnlm.antimine.IntentViewModelTest
import dev.lucasnlm.antimine.common.io.models.FirstOpen
import dev.lucasnlm.antimine.common.io.models.Save
import dev.lucasnlm.antimine.common.io.models.SaveStatus
import dev.lucasnlm.antimine.common.level.repository.SavesRepository
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.history.viewmodel.HistoryEvent
import dev.lucasnlm.antimine.history.viewmodel.HistoryState
import dev.lucasnlm.antimine.history.viewmodel.HistoryViewModel
import dev.lucasnlm.antimine.preferences.models.Minefield
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryViewModelTest : IntentViewModelTest() {
    private val fakeMinefield = Minefield(9, 9, 9)
    private val allSaves =
        listOf(
            Save(
                "0", 1, 0L, 100L, fakeMinefield,
                Difficulty.Beginner, FirstOpen.Unknown, SaveStatus.ON_GOING,
                listOf(), 10,
            ),
            Save(
                "1", 2, 0L, 100L, fakeMinefield,
                Difficulty.Beginner, FirstOpen.Unknown, SaveStatus.ON_GOING,
                listOf(), 20,
            ),
            Save(
                "2", 3, 0L, 100L, fakeMinefield,
                Difficulty.Beginner, FirstOpen.Unknown, SaveStatus.ON_GOING,
                listOf(), 30,
            ),
        )

    @Test
    fun testInitialValue() {
        val viewModel = HistoryViewModel(mockk(), mockk(), mockk())
        assertEquals(HistoryState(true, listOf()), viewModel.singleState())
    }

    @Test
    fun testLoadHistory() {
        val savesRepository =
            mockk<SavesRepository> {
                coEvery { getAllSaves() } returns allSaves
            }

        val state =
            HistoryViewModel(mockk(), savesRepository, mockk()).run {
                sendEvent(HistoryEvent.LoadAllSaves)
                singleState()
            }
        assertEquals(HistoryState(false, allSaves.sortedByDescending { it.startDate }), state)
    }
}
