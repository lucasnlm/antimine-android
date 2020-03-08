package dev.lucasnlm.antimine.about.thirds.view

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import dev.lucasnlm.antimine.R

import dev.lucasnlm.antimine.about.Constants
import dev.lucasnlm.antimine.about.TextActivity
import dev.lucasnlm.antimine.about.thirds.data.ThirdParty

internal class ThirdPartyAdapter(
    private val thirdParties: List<ThirdParty>
) : RecyclerView.Adapter<ThirdPartyItemHolder>() {

    override fun getItemCount(): Int = thirdParties.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThirdPartyItemHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.view_third_party, parent, false)
        return ThirdPartyItemHolder(view)
    }

    override fun onBindViewHolder(holder: ThirdPartyItemHolder, position: Int) {
        val thirdParty = thirdParties[position]
        holder.title.text = thirdParty.name
        holder.itemView.setOnClickListener { view ->
            val intent = Intent(view.context, TextActivity::class.java).apply {
                putExtra(Constants.TEXT_TITLE, thirdParty.name)
                putExtra(Constants.TEXT_PATH, thirdParty.license)
            }

            view.context.startActivity(intent)
        }
    }
}
