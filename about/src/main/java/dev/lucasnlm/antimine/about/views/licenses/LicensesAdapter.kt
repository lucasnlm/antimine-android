package dev.lucasnlm.antimine.about.views.licenses

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import dev.lucasnlm.antimine.about.R

import dev.lucasnlm.antimine.text.TextActivity
import dev.lucasnlm.antimine.about.viewmodel.License
import kotlinx.android.synthetic.main.view_third_party.view.*

internal class LicensesAdapter(
    private val licenses: List<License>,
) : RecyclerView.Adapter<ThirdPartyViewHolder>() {

    override fun getItemCount(): Int = licenses.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThirdPartyViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.view_third_party, parent, false)
        return ThirdPartyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThirdPartyViewHolder, position: Int) {
        val thirdParty = licenses[position]
        holder.apply {
            title.text = thirdParty.name
            itemView.setOnClickListener { view ->
                val intent = TextActivity.getIntent(view.context, thirdParty.name, thirdParty.licenseFileRes)
                view.context.startActivity(intent)
            }
        }
    }
}

class ThirdPartyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView = view.third_name
}
