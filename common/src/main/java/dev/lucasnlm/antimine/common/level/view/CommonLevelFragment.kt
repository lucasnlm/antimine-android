package dev.lucasnlm.antimine.common.level.view

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.widget.FixedGridLayoutManager
import javax.inject.Inject

abstract class CommonLevelFragment : Fragment() {
    @Inject
    lateinit var dimensionRepository: IDimensionRepository

    protected lateinit var viewModel: GameViewModel
    protected lateinit var recyclerGrid: RecyclerView
    protected lateinit var areaAdapter: AreaAdapter

    abstract val levelFragmentResId: Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(levelFragmentResId, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            viewModel = it.viewModels<GameViewModel>().value
            areaAdapter = AreaAdapter(it.applicationContext, viewModel)
        }
    }

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

        val typedValue = TypedValue()
        val actionBarHeight = if (context.theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
        } else {
            0
        }

        val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val bottom = if (resourceId > 0) { resources.getDimensionPixelSize(resourceId) } else 0

        val height = displayMetrics.heightPixels - actionBarHeight - bottom
        val recyclerViewHeight = (dimensionRepository.areaSize() * boardHeight)
        val separatorsHeight = (dimensionRepository.areaSeparator() * (boardHeight - 1))

        return ((height - recyclerViewHeight - separatorsHeight) / 2).coerceAtLeast(0.0f).toInt()
    }
}
