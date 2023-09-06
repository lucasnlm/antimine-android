package dev.lucasnlm.antimine.stats.view

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.databinding.ViewStatsItemBinding
import dev.lucasnlm.antimine.stats.model.StatsModel
import java.text.NumberFormat
import java.util.Locale

class StatsAdapter(
    private val statsList: List<StatsModel>,
) : RecyclerView.Adapter<StatsViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): StatsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return StatsViewHolder(
            binding = ViewStatsItemBinding.inflate(layoutInflater, parent, false),
        )
    }

    override fun onBindViewHolder(
        holder: StatsViewHolder,
        position: Int,
    ) {
        val stats = statsList[position]
        holder.binding.apply {
            if (stats.totalGames > 0) {
                val emptyText = "-"
                if (stats.title != 0) {
                    statsLabel.text = holder.itemView.context.getString(stats.title)
                    statsLabel.isVisible = true
                } else {
                    statsLabel.isVisible = false
                }
                minesCount.text = stats.mines.toL10nString()
                totalTime.text = formatTime(stats.totalTime)
                averageTime.text = formatTime(stats.averageTime)
                shortestTime.text = if (stats.shortestTime == 0L) emptyText else formatTime(stats.shortestTime)
                totalGames.text = stats.totalGames.toL10nString()
                performance.text = formatPercentage(stats.victory.toDouble() / stats.totalGames)
                openAreas.text = stats.openArea.toL10nString()
                victory.text = stats.victory.toL10nString()
                defeat.text = (stats.totalGames - stats.victory).toL10nString()
            } else {
                val emptyText = "-"
                statsLabel.isVisible = false
                totalGames.text = 0.toL10nString()
                minesCount.text = emptyText
                totalTime.text = emptyText
                averageTime.text = emptyText
                shortestTime.text = emptyText
                performance.text = emptyText
                openAreas.text = emptyText
                victory.text = emptyText
                defeat.text = emptyText
            }
        }
    }

    override fun getItemCount(): Int = statsList.size

    companion object {
        private fun Int.toL10nString() = String.format(Locale.getDefault(), "%d", this)

        private fun formatPercentage(value: Double) =
            NumberFormat.getPercentInstance().run {
                maximumFractionDigits = 2
                format(value)
            }

        private fun formatTime(durationSecs: Long) = DateUtils.formatElapsedTime(durationSecs)
    }
}

class StatsViewHolder(
    val binding: ViewStatsItemBinding,
) : RecyclerView.ViewHolder(binding.root)
