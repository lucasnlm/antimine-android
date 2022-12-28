package dev.lucasnlm.antimine.themes.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.themes.R
import dev.lucasnlm.antimine.themes.viewmodel.ThemeViewModel
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.ext.toInvertedAndroidColor
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.repository.Themes
import kotlinx.android.synthetic.main.view_theme.view.*

class ThemeAdapter(
    private val themeViewModel: ThemeViewModel,
    private val preferencesRepository: IPreferencesRepository,
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.view_theme, parent, false)

        return ThemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        val theme = themes[position]

        holder.itemView.apply {
            val selected = (theme.id == themeViewModel.singleState().currentTheme.id)

            covered.setBackgroundColor(theme.palette.covered.toAndroidColor())
            uncovered.setBackgroundColor(theme.palette.background.toAndroidColor())

            if (theme.name != null) {
                label.apply {
                    text = label.context.getString(theme.name!!)
                    setTextColor(theme.palette.background.toInvertedAndroidColor(200))
                    setBackgroundResource(android.R.color.transparent)
                    setCompoundDrawables(null, null, null, null)
                    visibility = View.VISIBLE
                }
            } else {
                label.apply {
                    setCompoundDrawables(null, null, null, null)
                    visibility = View.GONE
                }
            }

            cardTheme.apply {
                setStrokeColor(
                    MaterialColors.getColorStateListOrNull(
                        context,
                        if (selected) R.attr.colorTertiary else R.attr.backgroundColor,
                    ),
                )
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

class ThemeViewHolder(view: View) : RecyclerView.ViewHolder(view)
