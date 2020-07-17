package dev.lucasnlm.antimine.common.level.view

import android.util.TypedValue
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.widget.FixedGridLayoutManager
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import javax.inject.Inject

abstract class CommonLevelFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {
    @Inject
    lateinit var dimensionRepository: IDimensionRepository

    @Inject
    lateinit var preferencesRepository: IPreferencesRepository

    protected val viewModel: GameViewModel by activityViewModels()
    protected val areaAdapter by lazy { AreaAdapter(requireContext(), viewModel, preferencesRepository) }
    protected lateinit var recyclerGrid: RecyclerView

    protected fun makeNewLayoutManager(boardWidth: Int) =
        FixedGridLayoutManager().apply {
            setTotalColumnCount(boardWidth)
        }

    protected fun calcHorizontalPadding(boardWidth: Int): Int {
        val context = requireContext()
        val displayMetrics = context.resources.displayMetrics

        val width = displayMetrics.widthPixels
        val recyclerViewWidth = (dimensionRepository.areaSize() * boardWidth)
        val separatorsWidth = (dimensionRepository.areaSeparator() * (boardWidth - 1))
        return ((width - recyclerViewWidth - separatorsWidth) / 2).coerceAtLeast(0.0f).toInt()
    }

    protected fun calcVerticalPadding(boardHeight: Int): Int {
        val context = requireContext()
        val displayMetrics = context.resources.displayMetrics

        val typedValue = TypedValue()
        val actionBarHeight = if (context.theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
        } else {
            0
        }
        val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val navigationHeight = if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0

        val height = displayMetrics.heightPixels
        val recyclerViewHeight = (dimensionRepository.areaSize() * boardHeight)
        val separatorsHeight = (2 * dimensionRepository.areaSeparator() * (boardHeight - 1))

        val calculatedHeight = (height - actionBarHeight - navigationHeight - recyclerViewHeight - separatorsHeight)

        return (calculatedHeight / 2).coerceAtLeast(0.0f).toInt()
    }

    protected fun calcVerticalScrollToCenter(boardHeight: Int): Int {
        val context = requireContext()
        val displayMetrics = context.resources.displayMetrics

        val height = displayMetrics.heightPixels
        val recyclerViewHeight = (dimensionRepository.areaSize() * boardHeight)
        val separatorsHeight = (2 * dimensionRepository.areaSeparator() * (boardHeight - 1))

        return (((recyclerViewHeight + separatorsHeight) - height).coerceAtLeast(0.0f) * 0.5f).toInt()
    }

    protected fun calcHorizontalScrollToCenter(boardWidth: Int): Int {
        val context = requireContext()
        val displayMetrics = context.resources.displayMetrics

        val width = displayMetrics.widthPixels
        val recyclerViewWidth = (dimensionRepository.areaSize() * boardWidth)
        val separatorsWidth = (2 * dimensionRepository.areaSeparator() * (boardWidth - 1))

        return (((recyclerViewWidth + separatorsWidth) - width).coerceAtLeast(0.0f) * 0.5f).toInt()
    }
}
