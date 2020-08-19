package dev.lucasnlm.antimine.common.level.view

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.widget.FixedGridLayoutManager
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import org.koin.android.ext.android.inject

abstract class CommonLevelFragment(@LayoutRes val contentLayoutId: Int) : Fragment(contentLayoutId) {
    private val dimensionRepository: IDimensionRepository by inject()
    private val preferencesRepository: IPreferencesRepository by inject()
    protected val gameViewModel: GameViewModel by inject()
    protected val areaAdapter by lazy {
        AreaAdapter(requireContext(), gameViewModel, preferencesRepository, dimensionRepository)
    }
    protected lateinit var recyclerGrid: RecyclerView

    protected fun makeNewLayoutManager(boardWidth: Int) =
        FixedGridLayoutManager().apply {
            setTotalColumnCount(boardWidth)
        }

    protected fun calcHorizontalPadding(boardWidth: Int): Int {
        val width = requireView().measuredWidth
        val recyclerViewWidth = (dimensionRepository.areaSize() * boardWidth)
        val separatorsWidth = (dimensionRepository.areaSeparator() * (boardWidth - 1))
        return ((width - recyclerViewWidth - separatorsWidth) / 2).coerceAtLeast(0.0f).toInt()
    }

    protected fun calcVerticalPadding(boardHeight: Int): Int {
        val height = requireView().measuredHeight
        val recyclerViewHeight = (dimensionRepository.areaSize() * boardHeight)
        val separatorsHeight = (2 * dimensionRepository.areaSeparator() * (boardHeight - 1))
        val calculatedHeight = (height - recyclerViewHeight - separatorsHeight)
        return (calculatedHeight / 2).coerceAtLeast(0.0f).toInt()
    }
}
