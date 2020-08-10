package dev.lucasnlm.antimine.history.views

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.DeepLink
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.database.models.SaveStatus
import dev.lucasnlm.antimine.common.level.models.Difficulty
import kotlinx.android.synthetic.main.view_history_item.view.*

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
            holder.replay.setOnClickListener { replayGame(it, uid) }
        } else {
            holder.replay.setImageResource(R.drawable.play)
            holder.replay.setColorFilter(holder.minesCount.currentTextColor)
            holder.replay.setOnClickListener { loadGame(it, uid) }
        }

        holder.itemView.setOnClickListener { loadGame(it, uid) }
    }

    private fun replayGame(view: View, uid: Int) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = Uri.Builder()
                .scheme(DeepLink.SCHEME)
                .authority(DeepLink.RETRY_HOST_AUTHORITY)
                .appendPath(uid.toString())
                .build()
        }
        view.context.startActivity(intent)
    }

    private fun loadGame(view: View, uid: Int) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = Uri.Builder()
                .scheme(DeepLink.SCHEME)
                .authority(DeepLink.LOAD_GAME_AUTHORITY)
                .appendPath(uid.toString())
                .build()
        }
        view.context.startActivity(intent)
    }
}

class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val flag: AppCompatImageView = view.badge
    val difficulty: TextView = view.difficulty
    val minefieldSize: TextView = view.minefieldSize
    val minesCount: TextView = view.minesCount
    val replay: AppCompatImageView = view.replay
}
