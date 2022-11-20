package dev.lucasnlm.antimine.history.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
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
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.view_history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemId(position: Int): Long {
        return saveHistory[position].uid.toLong()
    }

    override fun getItemCount(): Int {
        return saveHistory.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) = with(saveHistory[position]) {
        val context = holder.itemView.context

        val difficultyText = context.getString(
            when (difficulty) {
                Difficulty.Beginner -> R.string.beginner
                Difficulty.Intermediate -> R.string.intermediate
                Difficulty.Expert -> R.string.expert
                Difficulty.Standard -> R.string.standard
                Difficulty.Master -> R.string.master
                Difficulty.Legend -> R.string.legend
                Difficulty.Custom -> R.string.custom
                Difficulty.FixedSize -> R.string.fixed_size
            },
        )

        val gameNameText = "$difficultyText #$uid"

        holder.difficulty.text = gameNameText

        holder.flag.alpha = if (status == SaveStatus.VICTORY) 1.0f else 0.5f

        holder.minefieldSize.text = String.format("%d x %d", minefield.width, minefield.height)
        holder.minesCount.text = context.getString(R.string.mines_remaining, minefield.mines)

        if (status != SaveStatus.VICTORY) {
            holder.replay.icon = ContextCompat.getDrawable(context, R.drawable.replay)
            holder.replay.setOnClickListener {
                statelessViewModel.sendEvent(HistoryEvent.ReplaySave(uid))
            }
        } else {
            holder.replay.icon = ContextCompat.getDrawable(context, R.drawable.play)
            holder.replay.setOnClickListener {
                statelessViewModel.sendEvent(HistoryEvent.ReplaySave(uid))
            }
        }

        holder.open.setOnClickListener {
            statelessViewModel.sendEvent(HistoryEvent.LoadSave(uid))
        }
    }
}

class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val flag: AppCompatImageView = view.badge
    val difficulty: TextView = view.difficulty
    val minefieldSize: TextView = view.minefieldSize
    val minesCount: TextView = view.minesCount
    val replay: MaterialButton = view.replay
    val open: MaterialButton = view.open
}
