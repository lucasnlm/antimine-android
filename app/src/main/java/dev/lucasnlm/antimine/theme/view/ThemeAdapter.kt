package dev.lucasnlm.antimine.theme.view

import android.content.Context
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
import dev.lucasnlm.antimine.theme.viewmodel.ThemeViewModel
import kotlinx.android.synthetic.main.view_theme.view.*

class ThemeAdapter(
    private val themeViewModel: ThemeViewModel,
    private val areaSize: Float
) : RecyclerView.Adapter<ThemeViewHolder>() {

    private val themes: List<AppTheme> = themeViewModel.getThemes()

    private val minefield = listOf(
        Area(0, 0, 0, 1, hasMine = false, mistake = false, mark = Mark.None, isCovered = false)
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
            area0.bindTheme(minefield[0], theme, paintSettings)
            area1.bindTheme(minefield[0], theme, paintSettings)
            area2.bindTheme(minefield[0], theme, paintSettings)
            area3.bindTheme(minefield[0], theme, paintSettings)
            area4.bindTheme(minefield[0], theme, paintSettings)
            area5.bindTheme(minefield[0], theme, paintSettings)
            area6.bindTheme(minefield[0], theme, paintSettings)
            area7.bindTheme(minefield[0], theme, paintSettings)
            area8.bindTheme(minefield[0], theme, paintSettings)

            clickTheme.setOnClickListener {
                themeViewModel.setTheme(theme)
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
