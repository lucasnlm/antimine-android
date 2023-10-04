package dev.lucasnlm.antimine.history.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.io.models.Save
import dev.lucasnlm.antimine.common.io.models.SaveStatus
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.viewmodel.StatelessViewModel
import dev.lucasnlm.antimine.databinding.ViewHistoryItemBinding
import dev.lucasnlm.antimine.history.viewmodel.HistoryEvent
import com.google.android.material.R as GR
import dev.lucasnlm.antimine.i18n.R as i18n

class HistoryAdapter(
    private val saveHistory: List<Save>,
    private val statelessViewModel: StatelessViewModel<HistoryEvent>,
) : RecyclerView.Adapter<HistoryViewHolder>() {
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): HistoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HistoryViewHolder(
            binding = ViewHistoryItemBinding.inflate(layoutInflater, parent, false),
        )
    }

    override fun getItemId(position: Int): Long {
        return saveHistory[position].id.hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return saveHistory.size
    }

    override fun onBindViewHolder(
        holder: HistoryViewHolder,
        position: Int,
    ) = with(saveHistory[position]) {
        val context = holder.itemView.context
        val buttonIconColor =
            MaterialColors.getColorStateListOrNull(
                context,
                GR.attr.colorTertiary,
            )?.withAlpha(BUTTON_BACKGROUND_ALPHA)

        val difficultyText =
            context.getString(
                when (difficulty) {
                    Difficulty.Beginner -> i18n.string.beginner
                    Difficulty.Intermediate -> i18n.string.intermediate
                    Difficulty.Expert -> i18n.string.expert
                    Difficulty.Standard -> i18n.string.standard
                    Difficulty.Master -> i18n.string.master
                    Difficulty.Legend -> i18n.string.legend
                    Difficulty.Custom -> i18n.string.custom
                    Difficulty.FixedSize -> i18n.string.fixed_size
                },
            )

        val saveId = id.orEmpty()

        holder.binding.run {
            difficulty.text = difficultyText
            badge.alpha = if (status == SaveStatus.VICTORY) BADGE_VICTORY_ALPHA else BADGE_DEFEAT_ALPHA

            minefieldSize.text = context.getString(i18n.string.minefield_size, minefield.width, minefield.height)
            minesCount.text = context.getString(i18n.string.mines_remaining, minefield.mines)

            replay.run {
                if (status != SaveStatus.VICTORY) {
                    icon = ContextCompat.getDrawable(context, R.drawable.replay)
                    setOnClickListener {
                        statelessViewModel.sendEvent(HistoryEvent.ReplaySave(saveId))
                    }
                    backgroundTintList = buttonIconColor
                } else {
                    icon = ContextCompat.getDrawable(context, R.drawable.play)
                    setOnClickListener {
                        statelessViewModel.sendEvent(HistoryEvent.ReplaySave(saveId))
                    }
                    backgroundTintList = buttonIconColor
                }
            }

            open.run {
                setOnClickListener {
                    statelessViewModel.sendEvent(HistoryEvent.LoadSave(saveId))
                }
                backgroundTintList = buttonIconColor
            }
        }
    }

    companion object {
        const val BUTTON_BACKGROUND_ALPHA = 50
        const val BADGE_VICTORY_ALPHA = 1.0f
        const val BADGE_DEFEAT_ALPHA = 0.5f
    }
}

class HistoryViewHolder(
    val binding: ViewHistoryItemBinding,
) : RecyclerView.ViewHolder(binding.root)
