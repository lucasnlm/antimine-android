package dev.lucasnlm.antimine.history.views

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.history.viewmodel.HistoryEvent
import dev.lucasnlm.antimine.history.viewmodel.HistoryViewModel
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryFragment : Fragment(R.layout.fragment_history) {
    private val historyViewModel by viewModel<HistoryViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenCreated {
            historyViewModel.sendEvent(HistoryEvent.LoadAllSaves)

            historyViewModel.observeState().collect {
                empty.visibility = if (!it.loading && it.saveList.isEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                loading.visibility = if (it.loading) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                saveHistory.apply {
                    layoutManager = LinearLayoutManager(view.context)
                    adapter = HistoryAdapter(it.saveList, historyViewModel)
                }
            }
        }
    }
}
