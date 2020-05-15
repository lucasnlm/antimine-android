package dev.lucasnlm.antimine.history.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.DaggerFragment
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.history.viewmodel.HistoryViewModel
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class HistoryFragment : DaggerFragment() {
    @Inject
    lateinit var savesRepository: ISavesRepository

    private var historyViewModel: HistoryViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.run {
            historyViewModel = ViewModelProviders.of(this).get(HistoryViewModel::class.java)
        }

        GlobalScope.launch {
            historyViewModel?.loadAllSaves(savesRepository)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_history, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saveHistory.apply {
            addItemDecoration(
                DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL)
            )
            layoutManager = LinearLayoutManager(view.context)

            historyViewModel?.saves?.observe(viewLifecycleOwner, Observer {
                adapter = HistoryAdapter(it)
            })
        }
    }
}
