package dev.lucasnlm.antimine.about.translators.view

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.R

internal class TranslatorsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val language: TextView = view.findViewById(R.id.language)
    val translators: TextView = view.findViewById(R.id.translators)
}
