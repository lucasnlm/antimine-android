package dev.lucasnlm.antimine.common.level.view

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.widget.FixedGridLayoutManager
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.math.nextDown

abstract class CommonLevelFragment(@LayoutRes val contentLayoutId: Int) : Fragment(contentLayoutId) {
    private val dimensionRepository: IDimensionRepository by inject()
    private val preferencesRepository: IPreferencesRepository by inject()
    protected val gameViewModel by sharedViewModel<GameViewModel>()
    protected val areaAdapter by lazy {
        AreaAdapter(requireContext(), gameViewModel, preferencesRepository, dimensionRepository)
    }
    protected lateinit var recyclerGrid: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerGrid = view.findViewById(R.id.recyclerGrid)
    }

    protected  open fun makeNewLayoutManager(boardWidth: Int): RecyclerView.LayoutManager =
        FixedGridLayoutManager().apply {
            setTotalColumnCount(boardWidth)
        }

    protected fun setupRecyclerViewSize(view: View, levelSetup: Minefield) {
        recyclerGrid.apply {
            val horizontalPadding = calcHorizontalPadding(view, levelSetup.width)
            val verticalPadding = calcVerticalPadding(view, levelSetup.height)
            setPadding(horizontalPadding, verticalPadding, 0, 0)
            layoutManager = makeNewLayoutManager(levelSetup.width)
            adapter = areaAdapter
        }
    }

    private fun calcHorizontalPadding(view: View, boardWidth: Int): Int {
        val width = view.measuredWidth
        val recyclerViewWidth = (dimensionRepository.areaSize() * boardWidth)
        val separatorsWidth = (dimensionRepository.areaSeparator() * (boardWidth + 2))
        return ((width - recyclerViewWidth - separatorsWidth) / 2).coerceAtLeast(0.0f).nextDown().toInt()
    }

    private fun calcVerticalPadding(view: View, boardHeight: Int): Int {
        val height = view.measuredHeight
        val hasAds = !preferencesRepository.isPremiumEnabled()
        val adsHeight = if (hasAds) dpFromPx(view.context, 60.0f) else 0

        val recyclerViewHeight = (dimensionRepository.areaSize() * boardHeight)
        val separatorsHeight = (2 * dimensionRepository.areaSeparator() * (boardHeight - 1))
        val calculatedHeight = (height - recyclerViewHeight - separatorsHeight - adsHeight)
        return ((calculatedHeight / 2) - adsHeight).coerceAtLeast(0.0f).toInt()
    }

    open fun dpFromPx(context: Context, px: Float): Int {
        return (px / context.resources.displayMetrics.density).toInt()
    }
}
