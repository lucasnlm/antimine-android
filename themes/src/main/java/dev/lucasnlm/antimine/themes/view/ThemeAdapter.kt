package dev.lucasnlm.antimine.themes.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.themes.R
import dev.lucasnlm.antimine.themes.databinding.ViewThemeBinding
import dev.lucasnlm.antimine.themes.viewmodel.ThemeViewModel
import dev.lucasnlm.antimine.ui.ext.ColorExt.toAndroidColor
import dev.lucasnlm.antimine.ui.ext.ColorExt.toInvertedAndroidColor
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.i18n.R as i18n

class ThemeAdapter(
    private val themeViewModel: ThemeViewModel,
    private val preferencesRepository: PreferencesRepository,
    private val onSelectTheme: (AppTheme) -> Unit,
    private val onRequestPurchase: () -> Unit,
) : RecyclerView.Adapter<ThemeViewHolder>() {

    private val themes: List<AppTheme> = themeViewModel.singleState().themes

    init {
        setHasStableIds(true)
        stateRestorationPolicy = StateRestorationPolicy.ALLOW
    }

    override fun getItemId(position: Int): Long = themes[position].id

    override fun getItemCount(): Int = themes.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ThemeViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ViewThemeBinding.inflate(layoutInflater, parent, false)
        return ThemeViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ThemeViewHolder,
        position: Int,
    ) {
        val theme = themes[position]

        holder.itemView.apply {
            isSoundEffectsEnabled = false

            val selected = (theme.id == themeViewModel.singleState().currentTheme.id)

            holder.binding.covered.apply {
                setBackgroundColor(theme.palette.covered.toAndroidColor())
                alpha = 1.0f
            }
            holder.binding.uncovered.apply {
                setBackgroundColor(theme.palette.background.toAndroidColor())
                alpha = 1.0f
            }

            if (selected) {
                holder.binding.label.apply {
                    text = context.getString(i18n.string.selected)
                    setTextColor(theme.palette.background.toInvertedAndroidColor(200))
                    setBackgroundResource(android.R.color.transparent)
                    setCompoundDrawables(null, null, null, null)
                    isVisible = true
                }
                holder.binding.covered.alpha = 0.25f
            } else if (theme.name != null) {
                holder.binding.label.apply {
                    text = context.getString(theme.name!!)
                    setTextColor(theme.palette.background.toInvertedAndroidColor(200))
                    setBackgroundResource(android.R.color.transparent)
                    setCompoundDrawables(null, null, null, null)
                    isVisible = true
                }
            } else {
                holder.binding.label.apply {
                    setCompoundDrawables(null, null, null, null)
                    isVisible = false
                }
            }

            holder.binding.cardTheme.apply {
                strokeColor = theme.palette.background.toAndroidColor()
                isSoundEffectsEnabled = false
                setOnClickListener {
                    if (preferencesRepository.isPremiumEnabled()) {
                        onSelectTheme(theme)
                    } else {
                        onRequestPurchase()
                    }
                }
            }
        }
    }
}

class ThemeViewHolder(
    val binding: ViewThemeBinding,
) : RecyclerView.ViewHolder(binding.root)
