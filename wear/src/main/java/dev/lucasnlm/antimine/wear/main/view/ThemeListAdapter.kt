package dev.lucasnlm.antimine.wear.main.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.ui.ext.ColorExt.toAndroidColor
import dev.lucasnlm.antimine.ui.ext.ColorExt.toInvertedAndroidColor
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.wear.R
import dev.lucasnlm.antimine.wear.databinding.ViewThemeBinding
import com.google.android.material.R as GR

class ThemeListAdapter(
    private val themes: List<AppTheme>,
    private val onSelectTheme: (AppTheme) -> Unit,
    private val preferencesRepository: PreferencesRepository,
) : RecyclerView.Adapter<ThemeListAdapter.RecyclerViewHolder>() {
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder {
        val binding = ViewThemeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecyclerViewHolder(
            binding = binding,
            preferencesRepository = preferencesRepository,
            onSelectTheme = onSelectTheme,
        )
    }

    override fun getItemCount(): Int {
        return themes.size
    }

    override fun getItemId(position: Int): Long {
        return themes[position].id
    }

    override fun onBindViewHolder(
        holder: RecyclerViewHolder,
        position: Int,
    ) {
        val theme = themes[position]
        holder.bind(theme)
    }

    class RecyclerViewHolder(
        private val binding: ViewThemeBinding,
        private val preferencesRepository: PreferencesRepository,
        private val onSelectTheme: (AppTheme) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(theme: AppTheme) {
            val selected = (theme.id == preferencesRepository.themeId())
            val themeName = theme.name

            binding.covered.setBackgroundColor(theme.palette.covered.toAndroidColor())
            binding.uncovered.setBackgroundColor(theme.palette.background.toAndroidColor())

            if (themeName != null) {
                binding.label.apply {
                    text = context.getString(themeName)
                    setTextColor(theme.palette.background.toInvertedAndroidColor(THEME_LABEL_ALPHA))
                    setBackgroundResource(android.R.color.transparent)
                    setCompoundDrawables(null, null, null, null)
                    isVisible = true
                }
            } else {
                binding.label.apply {
                    setCompoundDrawables(null, null, null, null)
                    isVisible = false
                }
            }

            binding.cardTheme.apply {
                setStrokeColor(
                    MaterialColors.getColorStateListOrNull(
                        context,
                        if (selected) GR.attr.colorTertiary else GR.attr.backgroundColor,
                    ),
                )
                setOnClickListener {
                    onSelectTheme(theme)
                }
            }
        }
    }

    companion object {
        private const val THEME_LABEL_ALPHA = 200
    }
}
