package dev.lucasnlm.antimine.stats

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.ui.ThematicActivity
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.antimine.stats.view.StatsAdapter
import dev.lucasnlm.antimine.stats.viewmodel.StatsEvent
import dev.lucasnlm.antimine.stats.viewmodel.StatsViewModel
import dev.lucasnlm.external.IInstantAppManager
import kotlinx.android.synthetic.main.activity_stats.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class StatsActivity : ThematicActivity(R.layout.activity_stats) {
    private val statsViewModel by viewModel<StatsViewModel>()
    private val instantAppManager: IInstantAppManager by inject()
    private val themeRepository: IThemeRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }

        lifecycleScope.launchWhenResumed {
            statsViewModel.sendEvent(StatsEvent.LoadStats)

            statsViewModel.observeState().collect {
                recyclerView.adapter = StatsAdapter(it.stats, themeRepository)
                empty.visibility = if (it.stats.isEmpty()) View.VISIBLE else View.GONE

                if (it.showAds && !instantAppManager.isEnabled(applicationContext)) {
                    ad_placeholder.visibility = View.VISIBLE
                    ad_placeholder.loadAd()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        statsViewModel.singleState().let {
            if (it.stats.isNotEmpty()) {
                menuInflater.inflate(R.menu.delete_icon_menu, menu)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.delete) {
            confirmAndDelete()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun confirmAndDelete() {
        AlertDialog.Builder(this)
            .setTitle(R.string.are_you_sure)
            .setMessage(R.string.delete_all_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete_all) { _, _ ->
                lifecycleScope.launch {
                    statsViewModel.sendEvent(StatsEvent.DeleteStats)
                }
            }
            .show()
    }
}
