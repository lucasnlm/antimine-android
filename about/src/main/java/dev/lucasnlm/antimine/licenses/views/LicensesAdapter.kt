package dev.lucasnlm.antimine.licenses.views

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.about.databinding.ViewThirdPartyBinding
import dev.lucasnlm.antimine.licenses.viewmodel.License

internal class LicensesAdapter(
    private val licenses: List<License>,
) : RecyclerView.Adapter<ThirdPartyViewHolder>() {

    override fun getItemCount(): Int = licenses.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ThirdPartyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ThirdPartyViewHolder(
            binding = ViewThirdPartyBinding.inflate(layoutInflater, parent, false),
        )
    }

    override fun onBindViewHolder(
        holder: ThirdPartyViewHolder,
        position: Int,
    ) {
        val thirdParty = licenses[position]
        holder.apply {
            binding.thirdName.text = thirdParty.name
            itemView.setOnClickListener { view ->
                runCatching {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(thirdParty.url))
                    view.context.startActivity(intent)
                }
            }
        }
    }
}

class ThirdPartyViewHolder(
    val binding: ViewThirdPartyBinding,
) : RecyclerView.ViewHolder(binding.root)
