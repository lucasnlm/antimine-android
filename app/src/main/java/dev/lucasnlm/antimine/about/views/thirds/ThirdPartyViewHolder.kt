package dev.lucasnlm.antimine.about.views.thirds

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView

import kotlinx.android.synthetic.main.view_third_party.view.*

class ThirdPartyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView = view.third_name
}
