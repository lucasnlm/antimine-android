package dev.lucasnlm.antimine.history.views

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.R

class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val flag: AppCompatImageView = view.findViewById(R.id.badge)
    val difficulty: TextView = view.findViewById(R.id.difficulty)
    val minefieldSize: TextView = view.findViewById(R.id.minefieldSize)
    val minesCount: TextView = view.findViewById(R.id.minesCount)
    val replay: AppCompatImageView = view.findViewById(R.id.replay)
}
