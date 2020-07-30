package dev.lucasnlm.antimine.about.views.translators

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_translator.view.*

class TranslatorsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val language: TextView = view.language
    val translators: TextView = view.translators
}
