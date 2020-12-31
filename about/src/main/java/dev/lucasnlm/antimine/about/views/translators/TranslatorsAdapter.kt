package dev.lucasnlm.antimine.about.views.translators

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.about.R
import dev.lucasnlm.antimine.about.viewmodel.TranslationInfo

class TranslatorsAdapter(
    private val translators: List<TranslationInfo>,
) : RecyclerView.Adapter<TranslatorsViewHolder>() {

    override fun getItemCount(): Int = translators.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslatorsViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.view_translator, parent, false)
        return TranslatorsViewHolder(view)
    }

    override fun onBindViewHolder(holder: TranslatorsViewHolder, position: Int) {
        with(translators[position]) {
            holder.language.text = language
            holder.translators.text = translators.joinToString("\n")
        }
    }
}
