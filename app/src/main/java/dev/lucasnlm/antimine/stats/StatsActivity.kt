package dev.lucasnlm.antimine.stats

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
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
        viewModel.statsObserver.observe(
            this,
            Observer {
                minesCount.text = it.mines.toString()
                totalTime.text = formatTime(it.duration)
                averageTime.text = formatTime(it.averageDuration)
                totalGames.text = it.totalGames.toString()
                performance.text = formatPercentage(100.0 * it.victory / it.totalGames)
                openAreas.text = it.openArea.toString()
                victory.text = it.victory.toString()
                defeat.text = (it.totalGames - it.victory).toString()

                invalidateOptionsMenu()
            }
        )

        GlobalScope.launch {
            viewModel.loadStats()
        }
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
            GlobalScope.launch {
                viewModel.deleteAll()
            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private fun formatPercentage(value: Double) =
            String.format("%.2f%%", value)

        private fun formatTime(durationSecs: Long) =
            String.format("%02d:%02d:%02d", durationSecs / 3600, durationSecs % 3600 / 60, durationSecs % 60)
    }
}
