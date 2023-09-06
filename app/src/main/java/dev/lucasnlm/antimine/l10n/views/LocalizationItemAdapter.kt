package dev.lucasnlm.antimine.l10n.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.databinding.ViewLocalizationItemBinding
import dev.lucasnlm.antimine.l10n.models.GameLanguage
import java.util.*

class LocalizationItemAdapter(
    private val gameLanguages: List<GameLanguage>,
    private val onSelectLanguage: (Locale) -> Unit,
) : RecyclerView.Adapter<LocalizationItemViewHolder>() {
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): LocalizationItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LocalizationItemViewHolder(
            binding = ViewLocalizationItemBinding.inflate(layoutInflater, parent, false),
        )
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return gameLanguages[position].id.toLong()
    }

    override fun getItemCount(): Int {
        return gameLanguages.size
    }

    override fun onBindViewHolder(
        holder: LocalizationItemViewHolder,
        position: Int,
    ) {
        holder.binding.language.apply {
            text = gameLanguages[position].name
            setOnClickListener {
                onSelectLanguage(gameLanguages[position].locale)
            }
        }
    }
}

class LocalizationItemViewHolder(
    val binding: ViewLocalizationItemBinding,
) : RecyclerView.ViewHolder(binding.root)
