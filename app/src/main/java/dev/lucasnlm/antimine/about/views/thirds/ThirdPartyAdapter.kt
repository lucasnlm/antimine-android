package dev.lucasnlm.antimine.about.views.thirds

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import dev.lucasnlm.antimine.R

import dev.lucasnlm.antimine.text.TextActivity
import dev.lucasnlm.antimine.about.models.ThirdParty
import kotlinx.android.synthetic.main.view_third_party.view.*

internal class ThirdPartyAdapter(
    private val thirdParties: List<ThirdParty>
) : RecyclerView.Adapter<ThirdPartyViewHolder>() {

    override fun getItemCount(): Int = thirdParties.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThirdPartyViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.view_third_party, parent, false)
        return ThirdPartyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThirdPartyViewHolder, position: Int) {
        val thirdParty = thirdParties[position]
        holder.apply {
            title.text = thirdParty.name
            itemView.setOnClickListener { view ->
                val intent = TextActivity.getIntent(view.context, thirdParty.name, thirdParty.license)
                view.context.startActivity(intent)
            }
        }
    }
}

class ThirdPartyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView = view.third_name
}
