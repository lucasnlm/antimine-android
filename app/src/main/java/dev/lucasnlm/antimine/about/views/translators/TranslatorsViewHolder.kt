package dev.lucasnlm.antimine.about.views.translators

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.R

class TranslatorsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val language: TextView = view.findViewById(R.id.language)
    val translators: TextView = view.findViewById(R.id.translators)
}
