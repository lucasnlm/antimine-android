package dev.lucasnlm.antimine.history.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.database.models.SaveStatus
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.viewmodel.StatelessViewModel
import dev.lucasnlm.antimine.history.viewmodel.HistoryEvent
import kotlinx.android.synthetic.main.view_history_item.view.*

class HistoryAdapter(
    private val saveHistory: List<Save>,
    private val statelessViewModel: StatelessViewModel<HistoryEvent>,
) : RecyclerView.Adapter<HistoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.view_history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int = saveHistory.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) = with(saveHistory[position]) {
        holder.difficulty.text = holder.itemView.context.getString(
            when (difficulty) {
                Difficulty.Beginner -> R.string.beginner
                Difficulty.Intermediate -> R.string.intermediate
                Difficulty.Expert -> R.string.expert
                Difficulty.Standard -> R.string.standard
                Difficulty.Custom -> R.string.custom
            }
        )

        val context = holder.itemView.context
        holder.flag.setColorFilter(holder.minesCount.currentTextColor)
        holder.flag.alpha = if (status == SaveStatus.VICTORY) 1.0f else 0.35f

        holder.minefieldSize.text = String.format("%d x %d", minefield.width, minefield.height)
        holder.minesCount.text = context.getString(R.string.mines_remaining, minefield.mines)

        if (status != SaveStatus.VICTORY) {
            holder.replay.setImageResource(R.drawable.replay)
            holder.replay.setColorFilter(holder.minesCount.currentTextColor)
            holder.replay.setOnClickListener {
                statelessViewModel.sendEvent(HistoryEvent.ReplaySave(uid))
            }
        } else {
            holder.replay.setImageResource(R.drawable.play)
            holder.replay.setColorFilter(holder.minesCount.currentTextColor)
            holder.replay.setOnClickListener {
                statelessViewModel.sendEvent(HistoryEvent.LoadSave(uid))
            }
        }

        holder.itemView.setOnClickListener {
            statelessViewModel.sendEvent(HistoryEvent.LoadSave(uid))
        }
    }
}

class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val flag: AppCompatImageView = view.badge
    val difficulty: TextView = view.difficulty
    val minefieldSize: TextView = view.minefieldSize
    val minesCount: TextView = view.minesCount
    val replay: AppCompatImageView = view.replay
}
