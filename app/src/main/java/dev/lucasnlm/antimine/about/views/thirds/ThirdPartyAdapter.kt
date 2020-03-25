package dev.lucasnlm.antimine.about.views.thirds

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import dev.lucasnlm.antimine.R

import dev.lucasnlm.antimine.about.Constants
import dev.lucasnlm.antimine.about.TextActivity
import dev.lucasnlm.antimine.about.models.ThirdParty

class ThirdPartyAdapter(
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
                val intent = Intent(view.context, TextActivity::class.java).apply {
                    putExtra(Constants.TEXT_TITLE, thirdParty.name)
                    putExtra(Constants.TEXT_PATH, thirdParty.license)
                }

                view.context.startActivity(intent)
            }
        }
    }
}
