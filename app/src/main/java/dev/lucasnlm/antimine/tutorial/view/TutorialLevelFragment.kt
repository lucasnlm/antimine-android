package dev.lucasnlm.antimine.tutorial.view

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.view.SpaceItemDecoration
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.tutorial.viewmodel.TutorialViewModel
import kotlinx.android.synthetic.main.fragment_tutorial_level.*
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class TutorialLevelFragment : Fragment(R.layout.fragment_tutorial_level) {
    private val dimensionRepository: IDimensionRepository by inject()
    private val preferencesRepository: IPreferencesRepository by inject()
    private val tutorialViewModel by viewModel<TutorialViewModel>()
    private val gameViewModel by sharedViewModel<GameViewModel>()
    private val areaAdapter by lazy {
        TutorialAreaAdapter(requireContext(), tutorialViewModel, preferencesRepository, dimensionRepository).apply {
            setClickEnabled(true)
        }
    }
    private lateinit var recyclerGrid: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerGrid = view.findViewById(R.id.recyclerGrid)

        recyclerGrid.apply {
            addItemDecoration(SpaceItemDecoration(R.dimen.field_padding))
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(view.context, 5)
            adapter = areaAdapter
        }

        lifecycleScope.launchWhenCreated {
            tutorialViewModel.run {
                field.observe(
                    viewLifecycleOwner,
                    { list ->
                        gameViewModel.stopClock()
                        gameViewModel.elapsedTimeSeconds.postValue(0L)
                        gameViewModel.mineCount.postValue(
                            list.count { it.hasMine }.coerceAtLeast(4) - list.count { it.mark.isFlag() }
                        )
                        areaAdapter.bindField(list)

                        if (tutorialState.value.completed) {
                            gameViewModel.eventObserver.postValue(Event.FinishTutorial)
                        }
                    }
                )
            }
        }

        lifecycleScope.launchWhenCreated {
            tutorialViewModel.tutorialState.collect {
                tutorial_top.apply {
                    text = it.topMessage
                    setTextColor(
                        Color.argb(
                            255,
                            Color.red(tutorial_top.currentTextColor),
                            Color.green(tutorial_top.currentTextColor),
                            Color.blue(tutorial_top.currentTextColor),
                        ),
                    )
                }
                tutorial_bottom.apply {
                    text = it.bottomMessage
                    setTextColor(
                        Color.argb(
                            255,
                            Color.red(tutorial_top.currentTextColor),
                            Color.green(tutorial_top.currentTextColor),
                            Color.blue(tutorial_top.currentTextColor),
                        ),
                    )
                }
            }
        }
    }

    companion object {
        val TAG = TutorialLevelFragment::class.simpleName
    }
}
