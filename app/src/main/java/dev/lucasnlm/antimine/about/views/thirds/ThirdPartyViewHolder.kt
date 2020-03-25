package dev.lucasnlm.antimine.about.views.thirds

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView

import dev.lucasnlm.antimine.R

class ThirdPartyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView = view.findViewById(R.id.third_name)
}
