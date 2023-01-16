package dev.lucasnlm.antimine.stats.view

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.stats.model.StatsModel
import kotlinx.android.synthetic.main.view_stats_item.view.*
import java.text.NumberFormat

class StatsAdapter(
    private val statsList: List<StatsModel>,
) : RecyclerView.Adapter<StatsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.view_stats_item, parent, false)
        return StatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        val stats = statsList[position]
        holder.apply {
            if (stats.totalGames > 0) {
                val emptyText = "-"
                if (stats.title != 0) {
                    statsLabel.text = holder.itemView.context.getString(stats.title)
                    statsLabel.visibility = View.VISIBLE
                } else {
                    statsLabel.visibility = View.GONE
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
                statsLabel.visibility = View.GONE
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
        private fun Int.toL10nString() = String.format("%d", this)

        private fun formatPercentage(value: Double) =
            NumberFormat.getPercentInstance().run {
                maximumFractionDigits = 2
                format(value)
            }

        private fun formatTime(durationSecs: Long) =
            DateUtils.formatElapsedTime(durationSecs)
    }
}

class StatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val statsLabel: TextView = itemView.statsLabel
    val totalGames: TextView = itemView.totalGames
    val minesCount: TextView = itemView.minesCount
    val totalTime: TextView = itemView.totalTime
    val averageTime: TextView = itemView.averageTime
    val shortestTime: TextView = itemView.shortestTime
    val openAreas: TextView = itemView.openAreas
    val performance: TextView = itemView.performance
    val victory: TextView = itemView.victory
    val defeat: TextView = itemView.defeat
}
