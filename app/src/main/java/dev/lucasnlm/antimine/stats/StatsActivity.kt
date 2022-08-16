package dev.lucasnlm.antimine.stats

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.stats.view.StatsAdapter
import dev.lucasnlm.antimine.stats.viewmodel.StatsEvent
import dev.lucasnlm.antimine.stats.viewmodel.StatsViewModel
import dev.lucasnlm.antimine.ui.ext.ThematicActivity
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import kotlinx.android.synthetic.main.activity_stats.*
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class StatsActivity : ThematicActivity(R.layout.activity_stats) {
    private val statsViewModel by viewModel<StatsViewModel>()
    private val themeRepository: IThemeRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindToolbar(statsViewModel.singleState().stats.isEmpty())

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }

        lifecycleScope.launchWhenResumed {
            statsViewModel.sendEvent(StatsEvent.LoadStats)

            statsViewModel.observeState().collect {
                recyclerView.adapter = StatsAdapter(it.stats, themeRepository)
                empty.visibility = if (it.stats.isEmpty()) View.VISIBLE else View.GONE
                bindToolbar(it.stats.isEmpty())
            }
        }
    }

    private fun bindToolbar(emptyStats: Boolean) {
        if (emptyStats) {
            section.bind(
                text = R.string.events,
                startButton = R.drawable.back_arrow,
                startDescription = R.string.back,
                startAction = {
                    finish()
                },
            )
        } else {
            section.bind(
                text = R.string.events,
                startButton = R.drawable.back_arrow,
                startDescription = R.string.back,
                startAction = {
                    finish()
                },
                endButton = R.drawable.delete,
                endDescription = R.string.delete_all,
                endAction = {
                    confirmAndDelete()
                },
            )
        }
    }

    private fun confirmAndDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.are_you_sure)
            .setMessage(R.string.delete_all_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete_all) { _, _ ->
                lifecycleScope.launch {
                    statsViewModel.sendEvent(StatsEvent.DeleteStats)
                }
                bindToolbar(true)
            }
            .show()
    }
}
