package dev.lucasnlm.antimine.history.views

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.models.Difficulty
import java.text.DateFormat

class HistoryAdapter(
    private val saveHistory: List<Save>
) : RecyclerView.Adapter<HistoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.view_history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int = saveHistory.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) = with(saveHistory[position]) {
        holder.difficulty.text = holder.itemView.context.getString(when (difficulty) {
            Difficulty.Beginner -> R.string.beginner
            Difficulty.Intermediate -> R.string.intermediate
            Difficulty.Expert -> R.string.expert
            Difficulty.Standard -> R.string.standard
            Difficulty.Custom -> R.string.custom
        })

        holder.minefieldSize.text = String.format("%d x %d", minefield.width, minefield.height)
        holder.minesCount.text = holder.itemView.context.getString(R.string.mines_remaining, minefield.mines)
        holder.date.text = DateFormat.getDateInstance().format(startDate)

        holder.itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                data = Uri.parse("antimine://load-game/$uid")
            }
            it.context.startActivity(intent)
        }
    }
}
