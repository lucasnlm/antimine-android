package dev.lucasnlm.antimine.stats

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerAppCompatActivity
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.stats.viewmodel.StatsViewModel
import kotlinx.android.synthetic.main.activity_stats.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatsActivity : DaggerAppCompatActivity() {
    @Inject
    lateinit var statsRepository: IStatsRepository

    private lateinit var viewModel: StatsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        setTitle(R.string.events)

        viewModel = ViewModelProviders.of(this).get(StatsViewModel::class.java)
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
            }
        )

        GlobalScope.launch {
            viewModel.loadStats(statsRepository)
        }
    }

    private fun formatPercentage(value: Double) =
        String.format("%.2f%%", value)

    private fun formatTime(durationSecs: Long) =
        String.format("%02d:%02d:%02d", durationSecs / 3600, durationSecs % 3600 / 60, durationSecs % 60)
}
