package dev.lucasnlm.antimine.licenses.views

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.about.R
import dev.lucasnlm.antimine.licenses.viewmodel.License
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
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(thirdParty.url))
                    view.context.startActivity(intent)
                } catch (ignored: Exception) {
                    // Fail to load browser or URL.
                }
            }
        }
    }
}

class ThirdPartyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView = view.third_name
}
