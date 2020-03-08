package dev.lucasnlm.antimine.wear

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.common.R
import dagger.android.support.DaggerFragment
import dev.lucasnlm.antimine.common.level.data.AmbientSettings
import dev.lucasnlm.antimine.common.level.data.GameEvent
import dev.lucasnlm.antimine.common.level.view.AreaAdapter
import dev.lucasnlm.antimine.common.level.view.UnlockedHorizontalScrollView
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WatchLevelFragment: DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: GameViewModelFactory

    private lateinit var root: ViewGroup
    private lateinit var viewModel: GameViewModel
    private lateinit var recyclerGrid: RecyclerView
    private lateinit var bidirectionalScroll: UnlockedHorizontalScrollView
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        root = view.findViewById(R.id.root)

        recyclerGrid = view.findViewById(R.id.recyclerGrid)
        recyclerGrid.apply {
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            adapter = areaAdapter
            alpha = 0.0f
        }

        bidirectionalScroll = view.findViewById(R.id.bidirectionalScroll)
        bidirectionalScroll.setTarget(recyclerGrid)

        GlobalScope.launch {
            val levelSetup = viewModel.onCreate()

            val width = levelSetup.width

            withContext(Dispatchers.Main) {
                view.post {
                    recyclerGrid.layoutManager =
                        GridLayoutManager(activity, width, RecyclerView.VERTICAL, false)

                    view.post {
                        recyclerGrid.scrollToPosition(areaAdapter.itemCount / 2)
                        bidirectionalScroll.scrollBy(recyclerGrid.width / 4, 0)
                        recyclerGrid.animate().apply {
                            alpha(1.0f)
                            duration = 1000
                        }.start()
                    }
                }
            }
        }

        viewModel.run {
            field.observe(viewLifecycleOwner, Observer {
                areaAdapter.bindField(it)
            })
            levelSetup.observe(viewLifecycleOwner, Observer {
                recyclerGrid.layoutManager =
                    GridLayoutManager(activity, it.width, RecyclerView.VERTICAL, false)
            })
            fieldRefresh.observe(viewLifecycleOwner, Observer {
                areaAdapter.notifyItemChanged(it)
            })
            eventObserver.observe(viewLifecycleOwner, Observer {
                if (it == GameEvent.StartNewGame) {
                    recyclerGrid.scrollToPosition(areaAdapter.itemCount / 2)
                }

                when (it) {
                    GameEvent.ResumeGameOver, GameEvent.GameOver,
                    GameEvent.Victory, GameEvent.ResumeVictory -> areaAdapter.setClickEnabled(false)
                    else -> areaAdapter.setClickEnabled(true)
                }
            })
        }
    }

    fun setAmbientMode(ambientSettings: AmbientSettings) {
        areaAdapter.apply {
            setAmbientMode(ambientSettings.isAmbientMode, ambientSettings.isLowBitAmbient)
            notifyDataSetChanged()
        }

        recyclerGrid.setBackgroundResource(
            if (ambientSettings.isAmbientMode) android.R.color.black else android.R.color.transparent
        )
    }
}
