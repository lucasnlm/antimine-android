package dev.lucasnlm.antimine.themes.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.AreaPaintSettings
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.themes.R
import dev.lucasnlm.antimine.themes.viewmodel.ThemeEvent
import dev.lucasnlm.antimine.themes.viewmodel.ThemeViewModel
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.ext.toInvertedAndroidColor
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.view.AreaView
import dev.lucasnlm.antimine.ui.view.createAreaPaintSettings
import kotlinx.android.synthetic.main.view_theme.view.*

class ThemeAdapter(
    private val themeViewModel: ThemeViewModel,
    private val areaSize: Float,
    private val preferencesRepository: IPreferencesRepository,
) : RecyclerView.Adapter<ThemeViewHolder>() {

    private val themes: List<AppTheme> = themeViewModel.singleState().themes
    private val minefield = ExampleField.getField()
    private val squareRadius: Int = preferencesRepository.squareRadius()

    init {
        setHasStableIds(true)
        stateRestorationPolicy = StateRestorationPolicy.ALLOW
    }

    override fun getItemId(position: Int): Long = themes[position].id

    override fun getItemCount(): Int = themes.size

    override fun getItemViewType(position: Int): Int = themes[position].id.toInt()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.view_theme, parent, false)

        return ThemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        val theme = themes[position]
        val paintSettings = createAreaPaintSettings(
            holder.itemView.context,
            areaSize,
            squareRadius
        )

        holder.itemView.run {
            val selected = (theme.id == themeViewModel.singleState().current.id)
            val areas = listOf(area0, area1, area2, area3, area4, area5, area6, area7, area8)

            if (selected) {
                areas.forEach { it.alpha = 0.25f }
            } else {
                areas.forEach { it.alpha = 1.0f }
            }

            areas.forEachIndexed { index, areaView ->
                areaView.apply {
                    bindTheme(minefield[index], theme, paintSettings)
                    isClickable = false
                    isFocusable = false
                    isPressed = false
                }
            }

            if (position == 0) {
                areas.forEach { it.alpha = 0.30f }

                label.apply {
                    text = label.context.getString(R.string.system)
                    setTextColor(theme.palette.background.toInvertedAndroidColor(200))
                    setBackgroundResource(android.R.color.transparent)
                    setCompoundDrawables(null, null, null, null)
                    visibility = View.VISIBLE
                }
            } else if (theme.isPaid && !preferencesRepository.isPremiumEnabled()) {
                areas.forEach { it.alpha = 0.30f }

                label.apply {
                    text = label.context.getString(R.string.unlock)
                    setTextColor(theme.palette.background.toInvertedAndroidColor(200))
                    setBackgroundResource(android.R.color.transparent)
                    compoundDrawables.forEach {
                        it?.setTint(theme.palette.background.toInvertedAndroidColor(200))
                    }
                    visibility = View.VISIBLE
                }
            } else {
                label.setCompoundDrawables(null, null, null, null)
                label.visibility = View.GONE
            }

            theme_background.setBackgroundColor(theme.palette.background.toAndroidColor())

            clickable.setOnClickListener {
                themeViewModel.sendEvent(ThemeEvent.ChangeTheme(theme))
            }

            card_theme.apply {
                setOnClickListener {
                    themeViewModel.sendEvent(ThemeEvent.ChangeTheme(theme))
                }
                strokeColor = if (selected) {
                    theme.palette.accent.toAndroidColor()
                } else {
                    0
                }
            }
        }
    }

    private fun AreaView.bindTheme(area: Area, theme: AppTheme, paintSettings: AreaPaintSettings) {
        bindField(
            area = area,
            theme = theme,
            isAmbientMode = false,
            isLowBitAmbient = false,
            paintSettings = paintSettings
        )
    }
}

class ThemeViewHolder(view: View) : RecyclerView.ViewHolder(view)
