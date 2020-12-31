package dev.lucasnlm.antimine.theme.view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.AreaPaintSettings
import dev.lucasnlm.antimine.common.level.view.AreaAdapter
import dev.lucasnlm.antimine.common.level.view.AreaView
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.theme.viewmodel.ThemeEvent
import dev.lucasnlm.antimine.theme.viewmodel.ThemeViewModel
import kotlinx.android.synthetic.main.view_theme.view.*

class ThemeAdapter(
    private val themeViewModel: ThemeViewModel,
    private val areaSize: Float,
    private val squareRadius: Int,
) : RecyclerView.Adapter<ThemeViewHolder>() {

    private val themes: List<AppTheme> = themeViewModel.singleState().themes
    private val minefield = ExampleField.getField()

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
        val paintSettings = AreaAdapter.createAreaPaintSettings(
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

            areas.forEachIndexed { index, areaView -> areaView.bindTheme(minefield[index], theme, paintSettings) }

            if (position == 0) {
                areas.forEach { it.alpha = 0.35f }

                label.apply {
                    text = label.context.getString(R.string.system)

                    setTextColor(
                        with(theme.palette.background) {
                            Color.rgb(255 - Color.red(this), 255 - Color.green(this), 255 - Color.blue(this))
                        }
                    )
                    setBackgroundResource(android.R.color.transparent)
                    visibility = View.VISIBLE
                }
            } else {
                label.visibility = View.GONE
            }

            val color = with(theme.palette.background) {
                Color.rgb(Color.red(this), Color.green(this), Color.blue(this))
            }
            parentGrid.setBackgroundColor(color)

            clickTheme.setOnClickListener {
                themeViewModel.sendEvent(ThemeEvent.ChangeTheme(theme))
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
