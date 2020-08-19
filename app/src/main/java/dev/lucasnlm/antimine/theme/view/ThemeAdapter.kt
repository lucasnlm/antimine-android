package dev.lucasnlm.antimine.theme.view

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.AreaPaintSettings
import dev.lucasnlm.antimine.common.level.models.Mark
import dev.lucasnlm.antimine.common.level.view.AreaView
import dev.lucasnlm.antimine.core.themes.model.AppTheme
import dev.lucasnlm.antimine.theme.viewmodel.ThemeEvent
import dev.lucasnlm.antimine.theme.viewmodel.ThemeViewModel
import kotlinx.android.synthetic.main.view_theme.view.*

class ThemeAdapter(
    private val themeViewModel: ThemeViewModel,
    private val areaSize: Float
) : RecyclerView.Adapter<ThemeViewHolder>() {

    private val themes: List<AppTheme> = themeViewModel.singleState().themes

    private val minefield = listOf(
        Area(0, 0, 0, 1, hasMine = false, mistake = false, mark = Mark.None, isCovered = false),
        Area(1, 1, 0, 0, hasMine = true, mistake = false, mark = Mark.None, isCovered = false),
        Area(2, 2, 0, 0, hasMine = true, mistake = false, mark = Mark.None, isCovered = true),
        Area(3, 0, 1, 2, hasMine = false, mistake = false, mark = Mark.None, isCovered = false),
        Area(4, 1, 1, 3, hasMine = false, mistake = false, mark = Mark.None, isCovered = false),
        Area(5, 2, 1, 3, hasMine = true, mistake = false, mark = Mark.Flag, isCovered = true),
        Area(6, 0, 2, 0, hasMine = true, mistake = false, mark = Mark.Question, isCovered = true),
        Area(7, 1, 2, 4, hasMine = false, mistake = false, mark = Mark.None, isCovered = false),
        Area(8, 2, 2, 0, hasMine = false, mistake = false, mark = Mark.None, isCovered = true)
    )

    init {
        setHasStableIds(true)
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
        val paintSettings = createAreaPaintSettings(holder.itemView.context, areaSize)
        holder.itemView.run {
            val selected = (theme.id == themeViewModel.singleState().current.id)
            val areas = listOf(area0, area1, area2, area3, area4, area5, area6, area7, area8)

            if (selected) {
                areas.forEach { it.alpha = 0.25f }
            } else {
                areas.forEach { it.alpha = 1.0f }
            }

            areas.forEachIndexed { index, areaView -> areaView.bindTheme(minefield[index], theme, paintSettings) }

            if (position < 2 && !selected) {
                areas.forEach { it.alpha = 0.35f }

                label.apply {
                    text = if (position == 0) {
                        label.context.getString(R.string.system)
                    } else {
                        label.context.getString(R.string.amoled)
                    }

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

    companion object {
        fun createAreaPaintSettings(context: Context, size: Float): AreaPaintSettings {
            val resources = context.resources
            return AreaPaintSettings(
                Paint().apply {
                    isAntiAlias = true
                    isDither = true
                    style = Paint.Style.FILL
                    textSize = 18.0f * context.resources.displayMetrics.density
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                },
                RectF(0.0f, 0.0f, size, size),
                resources.getDimension(dev.lucasnlm.antimine.common.R.dimen.field_radius)
            )
        }
    }
}

class ThemeViewHolder(view: View) : RecyclerView.ViewHolder(view)
