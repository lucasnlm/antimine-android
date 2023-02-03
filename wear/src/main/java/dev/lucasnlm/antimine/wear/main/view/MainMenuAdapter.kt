package dev.lucasnlm.antimine.wear.main.view

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import dev.lucasnlm.antimine.wear.R
import dev.lucasnlm.antimine.wear.databinding.ViewMenuItemBinding
import dev.lucasnlm.antimine.wear.main.models.MenuItem

class MainMenuAdapter(
    private val menuItems: List<MenuItem>,
) : RecyclerView.Adapter<MainMenuAdapter.RecyclerViewHolder>() {
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val binding = ViewMenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecyclerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }

    override fun getItemId(position: Int): Long {
        return menuItems[position].id
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.bind(menuItem)
    }

    class RecyclerViewHolder(
        private val binding: ViewMenuItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(menuItem: MenuItem) {
            val context = binding.root.context

            binding.menuItem.apply {
                isAllCaps = false
                text = context.getString(menuItem.label)
                setTypeface(null, Typeface.NORMAL)
                setIconResource(menuItem.icon)
                backgroundTintList = if (menuItem.highlight) {
                    MaterialColors.getColorStateListOrNull(
                        context,
                        R.attr.colorPrimaryDark,
                    )?.withAlpha(50)
                } else {
                    MaterialColors.getColorStateListOrNull(
                        context,
                        R.attr.colorSurface,
                    )?.withAlpha(0)
                }
                setOnClickListener {
                    menuItem.onClick()
                }
            }
        }
    }
}
