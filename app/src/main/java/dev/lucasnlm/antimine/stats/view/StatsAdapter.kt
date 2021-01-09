package dev.lucasnlm.antimine.stats.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.antimine.stats.model.StatsModel
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.ext.toInvertedAndroidColor
import kotlinx.android.synthetic.main.view_stats.view.*

class StatsAdapter(
    private val statsList: List<StatsModel>,
    private val themeRepository: IThemeRepository,
) : RecyclerView.Adapter<StatsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.view_stats, parent, false)
        return StatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        val stats = statsList[position]
        holder.apply {
            val color = themeRepository.getTheme().palette.background.toAndroidColor()
            card.setCardBackgroundColor(color)

            val textColor = themeRepository.getTheme().palette.background.toInvertedAndroidColor()
            statsLabel.setTextColor(textColor)

            if (stats.totalGames > 0) {
                val emptyText = "-"
                if (stats.title != 0) {
                    statsLabel.text = holder.itemView.context.getString(stats.title)
                    statsLabel.visibility = View.VISIBLE
                } else {
                    statsLabel.visibility = View.GONE
                }
                minesCount.text = stats.mines.toString()
                totalTime.text = formatTime(stats.totalTime)
                averageTime.text = formatTime(stats.averageTime)
                shortestTime.text = if (stats.shortestTime == 0L) emptyText else formatTime(stats.shortestTime)
                totalGames.text = stats.totalGames.toString()
                performance.text = formatPercentage(100.0 * stats.victory / stats.totalGames)
                openAreas.text = stats.openArea.toString()
                victory.text = stats.victory.toString()
                defeat.text = (stats.totalGames - stats.victory).toString()
            } else {
                val emptyText = "-"
                statsLabel.visibility = View.GONE
                totalGames.text = "0"
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
        private fun formatPercentage(value: Double) =
            String.format("%.2f%%", value)

        private fun formatTime(durationSecs: Long) =
            String.format("%02d:%02d:%02d", durationSecs / 3600, durationSecs % 3600 / 60, durationSecs % 60)
    }
}

class StatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val card: CardView = itemView.card
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
