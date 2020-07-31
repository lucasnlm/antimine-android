package dev.lucasnlm.antimine.history.views

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_history_item.view.*

class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val flag: AppCompatImageView = view.badge
    val difficulty: TextView = view.difficulty
    val minefieldSize: TextView = view.minefieldSize
    val minesCount: TextView = view.minesCount
    val replay: AppCompatImageView = view.replay
}
