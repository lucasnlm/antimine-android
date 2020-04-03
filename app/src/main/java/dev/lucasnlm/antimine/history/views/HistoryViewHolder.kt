package dev.lucasnlm.antimine.history.views

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.R

class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val difficulty: TextView = view.findViewById(R.id.difficulty)
    val minefieldSize: TextView = view.findViewById(R.id.minefieldSize)
    val minesCount: TextView = view.findViewById(R.id.minesCount)
    val date: TextView = view.findViewById(R.id.date)
}
