package dev.lucasnlm.antimine.stats

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.stats.model.StatsModel
import dev.lucasnlm.antimine.stats.viewmodel.StatsViewModel
import kotlinx.android.synthetic.main.activity_stats.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StatsActivity : AppCompatActivity(R.layout.activity_stats) {
    @Inject
    lateinit var statsRepository: IStatsRepository

    private val viewModel: StatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshStats(StatsViewModel.emptyStats)

        viewModel.statsObserver.observe(
            this,
            Observer {
                refreshStats(it)
            }
        )

        GlobalScope.launch {
            viewModel.loadStats()
        }
    }

    private fun refreshStats(stats: StatsModel) {
        if (stats.totalGames > 0) {
            minesCount.text = stats.mines.toString()
            totalTime.text = formatTime(stats.duration)
            averageTime.text = formatTime(stats.averageDuration)
            totalGames.text = stats.totalGames.toString()
            performance.text = formatPercentage(100.0 * stats.victory / stats.totalGames)
            openAreas.text = stats.openArea.toString()
            victory.text = stats.victory.toString()
            defeat.text = (stats.totalGames - stats.victory).toString()
        } else {
            val emptyText = "-"
            totalGames.text = "0"
            minesCount.text = emptyText
            totalTime.text = emptyText
            averageTime.text = emptyText
            performance.text = emptyText
            openAreas.text = emptyText
            victory.text = emptyText
            defeat.text = emptyText
        }

        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        viewModel.statsObserver.value?.let {
            if (it.totalGames > 0) {
                menuInflater.inflate(R.menu.stats_menu, menu)
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
                GlobalScope.launch {
                    viewModel.deleteAll()
                }
            }
            .show()
    }


    companion object {
        private fun formatPercentage(value: Double) =
            String.format("%.2f%%", value)

        private fun formatTime(durationSecs: Long) =
            String.format("%02d:%02d:%02d", durationSecs / 3600, durationSecs % 3600 / 60, durationSecs % 60)
    }
}
