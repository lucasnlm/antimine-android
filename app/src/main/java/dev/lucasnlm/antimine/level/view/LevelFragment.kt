package dev.lucasnlm.antimine.level.view

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import dev.lucasnlm.antimine.DeepLink
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.view.AreaAdapter
import dev.lucasnlm.antimine.common.level.view.SpaceItemDecoration
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import dev.lucasnlm.antimine.common.level.widget.FixedGridLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

open class LevelFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: GameViewModelFactory

    @Inject
    lateinit var dimensionRepository: IDimensionRepository

    private lateinit var viewModel: GameViewModel
    private lateinit var recyclerGrid: RecyclerView
    private lateinit var areaAdapter: AreaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_level, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it, viewModelFactory).get(GameViewModel::class.java)
            areaAdapter = AreaAdapter(it.applicationContext, viewModel)
        }
    }

    override fun onPause() {
        super.onPause()

        GlobalScope.launch {
            viewModel.saveGame()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerGrid = view.findViewById(R.id.recyclerGrid)

        GlobalScope.launch {
            val loadGameUid = checkLoadGameDeepLink()
            val newGameDeepLink = checkNewGameDeepLink()
            val retryDeepLink = checkRetryGameDeepLink()

            val levelSetup = when {
                loadGameUid != null -> viewModel.loadGame(loadGameUid)
                newGameDeepLink != null -> viewModel.startNewGame(newGameDeepLink)
                retryDeepLink != null -> viewModel.retryGame(retryDeepLink)
                else -> viewModel.loadLastGame()
            }

            withContext(Dispatchers.Main) {
                recyclerGrid.apply {
                    val horizontalPadding = calcHorizontalPadding(levelSetup.width)
                    val verticalPadding = calcVerticalPadding(levelSetup.height)
                    setHasFixedSize(true)
                    addItemDecoration(SpaceItemDecoration(R.dimen.field_padding))
                    setPadding(horizontalPadding, verticalPadding, 0, 0)
                    layoutManager = makeNewLayoutManager(levelSetup.width)
                    adapter = areaAdapter
                    alpha = 0.0f

                    animate().apply {
                        alpha(1.0f)
                        duration = DateUtils.SECOND_IN_MILLIS
                    }.start()
                }
            }
        }

        viewModel.run {
            field.observe(
                viewLifecycleOwner,
                Observer {
                    areaAdapter.bindField(it)
                }
            )

            levelSetup.observe(
                viewLifecycleOwner,
                Observer {
                    recyclerGrid.apply {
                        val horizontalPadding = calcHorizontalPadding(it.width)
                        val verticalPadding = calcVerticalPadding(it.height)
                        layoutManager = makeNewLayoutManager(it.width)
                        setPadding(horizontalPadding, verticalPadding, 0, 0)
                    }
                }
            )

            fieldRefresh.observe(
                viewLifecycleOwner,
                Observer {
                    areaAdapter.notifyItemChanged(it)
                }
            )

            eventObserver.observe(
                viewLifecycleOwner,
                Observer {
                    when (it) {
                        Event.ResumeGameOver,
                        Event.GameOver,
                        Event.Victory,
                        Event.ResumeVictory -> areaAdapter.setClickEnabled(false)
                        Event.Running,
                        Event.ResumeGame,
                        Event.StartNewGame -> areaAdapter.setClickEnabled(true)
                        else -> {
                        }
                    }
                }
            )
        }
    }

    private fun checkNewGameDeepLink(): Difficulty? = activity?.intent?.data?.let { uri ->
        if (uri.scheme == DeepLink.SCHEME && uri.authority == DeepLink.NEW_GAME_AUTHORITY) {
            when (uri.pathSegments.firstOrNull()) {
                DeepLink.BEGINNER_PATH -> Difficulty.Beginner
                DeepLink.INTERMEDIATE_PATH -> Difficulty.Intermediate
                DeepLink.EXPERT_PATH -> Difficulty.Expert
                DeepLink.STANDARD_PATH -> Difficulty.Standard
                else -> null
            }
        } else {
            null
        }
    }

    private fun checkLoadGameDeepLink(): Int? = activity?.intent?.data?.let { uri ->
        if (uri.scheme == DeepLink.SCHEME && uri.authority == DeepLink.LOAD_GAME_AUTHORITY) {
            uri.pathSegments.firstOrNull()?.toIntOrNull()
        } else {
            null
        }
    }

    private fun checkRetryGameDeepLink(): Int? = activity?.intent?.data?.let { uri ->
        if (uri.scheme == DeepLink.SCHEME && uri.authority == DeepLink.RETRY_HOST_AUTHORITY) {
            uri.pathSegments.firstOrNull()?.toIntOrNull()
        } else {
            null
        }
    }

    private fun makeNewLayoutManager(boardWidth: Int) =
        FixedGridLayoutManager().apply {
            setTotalColumnCount(boardWidth)
        }

    private fun calcHorizontalPadding(boardWidth: Int): Int {
        val width = recyclerGrid.measuredWidth
        val recyclerViewWidth = (dimensionRepository.areaSize() * boardWidth)
        val separatorsWidth = (dimensionRepository.areaSeparator() * (boardWidth - 1))
        return ((width - recyclerViewWidth - separatorsWidth) / 2).coerceAtLeast(0.0f).toInt()
    }

    private fun calcVerticalPadding(boardHeight: Int): Int {
        val height = recyclerGrid.measuredHeight
        val recyclerViewHeight = (dimensionRepository.areaSize() * boardHeight)
        val separatorsHeight = (dimensionRepository.areaSeparator() * (boardHeight - 1))
        return ((height - recyclerViewHeight - separatorsHeight) / 2).coerceAtLeast(0.0f).toInt()
    }
}
