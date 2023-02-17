package dev.lucasnlm.antimine.history.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.database.models.SaveStatus
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.viewmodel.StatelessViewModel
import dev.lucasnlm.antimine.databinding.ViewHistoryItemBinding
import dev.lucasnlm.antimine.history.viewmodel.HistoryEvent

class HistoryAdapter(
    private val saveHistory: List<Save>,
    private val statelessViewModel: StatelessViewModel<HistoryEvent>,
) : RecyclerView.Adapter<HistoryViewHolder>() {
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HistoryViewHolder(
            binding = ViewHistoryItemBinding.inflate(layoutInflater, parent, false),
        )
    }

    override fun getItemId(position: Int): Long {
        return saveHistory[position].uid.toLong()
    }

    override fun getItemCount(): Int {
        return saveHistory.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) = with(saveHistory[position]) {
        val context = holder.itemView.context
        val buttonBackgroundColor = MaterialColors.getColorStateListOrNull(
            context,
            dev.lucasnlm.antimine.control.R.attr.colorOnBackground,
        )?.withAlpha(50)

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

        holder.binding.difficulty.text = gameNameText

        holder.binding.badge.alpha = if (status == SaveStatus.VICTORY) 1.0f else 0.5f

        holder.binding.minefieldSize.text = String.format("%d x %d", minefield.width, minefield.height)
        holder.binding.minesCount.text = context.getString(R.string.mines_remaining, minefield.mines)

        if (status != SaveStatus.VICTORY) {
            holder.binding.replay.icon = ContextCompat.getDrawable(context, R.drawable.replay)
            holder.binding.replay.setOnClickListener {
                statelessViewModel.sendEvent(HistoryEvent.ReplaySave(uid))
            }
            holder.binding.replay.backgroundTintList = buttonBackgroundColor
        } else {
            holder.binding.replay.icon = ContextCompat.getDrawable(context, R.drawable.play)
            holder.binding.replay.setOnClickListener {
                statelessViewModel.sendEvent(HistoryEvent.ReplaySave(uid))
            }
            holder.binding.replay.backgroundTintList = buttonBackgroundColor
        }

        holder.binding.open.setOnClickListener {
            statelessViewModel.sendEvent(HistoryEvent.LoadSave(uid))
        }
        holder.binding.open.backgroundTintList = buttonBackgroundColor
    }
}

class HistoryViewHolder(
    val binding: ViewHistoryItemBinding,
) : RecyclerView.ViewHolder(binding.root)
