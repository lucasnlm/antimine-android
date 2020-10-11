package dev.lucasnlm.antimine.history.views

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.history.viewmodel.HistoryEvent
import dev.lucasnlm.antimine.history.viewmodel.HistoryViewModel
import dev.lucasnlm.external.IInstantAppManager
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.fragment_history.view.*
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryFragment : Fragment(R.layout.fragment_history) {
    private val historyViewModel by viewModel<HistoryViewModel>()

    private val instantAppManager: IInstantAppManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenCreated {
            historyViewModel.sendEvent(HistoryEvent.LoadAllSaves)

            historyViewModel.observeState().collect {
                empty.visibility = if (it.saveList.isEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                saveHistory.apply {
                    addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
                    layoutManager = LinearLayoutManager(view.context)
                    adapter = HistoryAdapter(it.saveList, historyViewModel)
                }

                if (it.showAds && !instantAppManager.isEnabled(view.context)) {
                    view.ad_placeholder.visibility = View.VISIBLE
                    view.ad_placeholder.loadAd()
                }
            }
        }
    }
}
