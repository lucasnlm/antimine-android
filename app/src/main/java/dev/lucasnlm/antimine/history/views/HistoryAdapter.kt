package dev.lucasnlm.antimine.history.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel

class HistoryAdapter(
    private val saveHistory: List<Save>,
    private val gameViewModel: GameViewModel
) : RecyclerView.Adapter<HistoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.view_history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int = saveHistory.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) = with (saveHistory[position]) {
        holder.difficulty.text = holder.itemView.context.getString(when (difficulty) {
            Difficulty.Beginner -> R.string.beginner
            Difficulty.Intermediate -> R.string.intermediate
            Difficulty.Expert -> R.string.expert
            Difficulty.Standard -> R.string.standard
            Difficulty.Custom -> R.string.custom
        })

        holder.minefieldSize.text = String.format("%d x %d", minefield.width, minefield.height)
        //holder.minesCount.text = holder.itemView.context.getString(R.string.mines_remaining, minefield.mines)

        holder.itemView.setOnClickListener {
            gameViewModel.resumeGameFromSave(this)
        }
    }
}
