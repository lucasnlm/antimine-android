package dev.lucasnlm.antimine.level.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.common.R
import dagger.android.support.DaggerFragment
import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.view.AreaAdapter
import dev.lucasnlm.antimine.common.level.view.SpaceItemDecoration
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import dev.lucasnlm.antimine.level.widget.FixedGridLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

open class LevelFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: GameViewModelFactory

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
            val levelSetup = viewModel.onCreate(handleNewGameDeeplink())

            withContext(Dispatchers.Main) {
                recyclerGrid.apply {
                    setHasFixedSize(true)
                    isNestedScrollingEnabled = false
                    addItemDecoration(SpaceItemDecoration(R.dimen.field_padding))
                    layoutManager = FixedGridLayoutManager().apply {
                        setTotalColumnCount(levelSetup.width)
                    }
                    adapter = areaAdapter
                    alpha = 0.0f

                    animate().apply {
                        alpha(1.0f)
                        duration = 1000
                    }.start()
                }
            }
        }

        viewModel.run {
            field.observe(viewLifecycleOwner, Observer {
                areaAdapter.bindField(it)
            })

            levelSetup.observe(viewLifecycleOwner, Observer {
                recyclerGrid.layoutManager = FixedGridLayoutManager().apply {
                    setTotalColumnCount(it.width)
                }
            })

            fieldRefresh.observe(viewLifecycleOwner, Observer {
                areaAdapter.notifyItemChanged(it)
            })

            eventObserver.observe(viewLifecycleOwner, Observer {
                when (it) {
                    Event.ResumeGameOver,
                    Event.GameOver,
                    Event.Victory,
                    Event.ResumeVictory -> areaAdapter.setClickEnabled(false)
                    Event.Running,
                    Event.ResumeGame,
                    Event.StartNewGame -> areaAdapter.setClickEnabled(true)
                    else -> { }
                }
            })
        }
    }

    private fun handleNewGameDeeplink(): Difficulty? {
        var result: Difficulty? = null

        activity?.intent?.data?.let { uri ->
            if (uri.scheme == DEFAULT_SCHEME) {
                result = when (uri.schemeSpecificPart.removePrefix("//new-game/")) {
                    "beginner" -> Difficulty.Beginner
                    "intermediate" -> Difficulty.Intermediate
                    "expert" -> Difficulty.Expert
                    "standard" -> Difficulty.Standard
                    else -> null
                }
            }
        }

        return result
    }

    companion object {
        const val DEFAULT_SCHEME = "antimine"
    }
}
