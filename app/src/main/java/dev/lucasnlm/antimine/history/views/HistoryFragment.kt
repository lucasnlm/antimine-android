package dev.lucasnlm.antimine.history.views

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.history.viewmodel.HistoryViewModel
import kotlinx.android.synthetic.main.fragment_history.*
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.fragment_history) {
    @Inject
    lateinit var savesRepository: ISavesRepository

    private val historyViewModel: HistoryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saveHistory.apply {
            addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(view.context)
            historyViewModel.saves.observe(
                viewLifecycleOwner,
                Observer {
                    adapter = HistoryAdapter(it)
                }
            )
        }

        historyViewModel.saves.observe(
            viewLifecycleOwner,
            Observer {
                saveHistory.adapter = HistoryAdapter(it)
            }
        )
    }
}
