package dev.lucasnlm.antimine.about.thirds.view

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView

import dev.lucasnlm.antimine.R

internal class ThirdPartyItemHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView = view.findViewById(R.id.third_name)
}
