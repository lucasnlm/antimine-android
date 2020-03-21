package dev.lucasnlm.antimine.common.level.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.common.R

class AreaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    init {
        this.itemView.isFocusable = true
    }
}
