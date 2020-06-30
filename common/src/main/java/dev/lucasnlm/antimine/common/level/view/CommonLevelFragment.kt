package dev.lucasnlm.antimine.common.level.view

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.widget.FixedGridLayoutManager
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import javax.inject.Inject

abstract class CommonLevelFragment : Fragment() {
    @Inject
    lateinit var dimensionRepository: IDimensionRepository
    @Inject
    lateinit var preferencesRepository: IPreferencesRepository

    protected val viewModel: GameViewModel by activityViewModels()
    protected val areaAdapter by lazy { AreaAdapter(requireContext(), viewModel, preferencesRepository) }
    protected lateinit var recyclerGrid: RecyclerView

    abstract val levelFragmentResId: Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(levelFragmentResId, container, false)

    protected fun makeNewLayoutManager(boardWidth: Int) =
        FixedGridLayoutManager().apply {
            setTotalColumnCount(boardWidth)
        }

    protected fun calcHorizontalPadding(boardWidth: Int): Int {
        val windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val width = displayMetrics.widthPixels
        val recyclerViewWidth = (dimensionRepository.areaSize() * boardWidth)
        val separatorsWidth = (dimensionRepository.areaSeparator() * (boardWidth - 1))
        return ((width - recyclerViewWidth - separatorsWidth) / 2).coerceAtLeast(0.0f).toInt()
    }

    protected fun calcVerticalPadding(boardHeight: Int): Int {
        val context = requireContext()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val height = recyclerGrid.measuredHeight
        val recyclerViewHeight = (dimensionRepository.areaSize() * boardHeight)
        val separatorsHeight = (2 * dimensionRepository.areaSeparator() * (boardHeight - 1))

        return ((height - recyclerViewHeight - separatorsHeight) / 2).coerceAtLeast(0.0f).toInt()
    }
}
