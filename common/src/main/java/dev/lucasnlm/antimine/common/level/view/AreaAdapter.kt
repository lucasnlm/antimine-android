package dev.lucasnlm.antimine.common.level.view

import android.content.Context
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.data.Area
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel

class AreaAdapter(
    context: Context,
    private val viewModel: GameViewModel
) : RecyclerView.Adapter<FieldViewHolder>() {

    private var field = listOf<Area>()
    private var isLowBitAmbient = false
    private var isAmbientMode = false
    private val paintSettings: AreaPaintSettings

    private val clickEnabled: Boolean
        get() = viewModel.isGameActive()

    init {
        setHasStableIds(true)
        paintSettings = createAreaPaintSettings(context, viewModel.useAccessibilityMode())
    }

    fun setAmbientMode(isAmbientMode: Boolean, isLowBitAmbient: Boolean) {
        this.isLowBitAmbient = isLowBitAmbient
        this.isAmbientMode = isAmbientMode
    }

    fun bindField(area: List<Area>) {
        this.field = area
        notifyDataSetChanged()
    }

    override fun getItemCount() = field.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FieldViewHolder {
        val layout = if (viewModel.useAccessibilityMode()) { R.layout.view_accessibility_field } else { R.layout.view_field }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        val holder = FieldViewHolder(view)

        holder.itemView.setOnLongClickListener { target ->
            target.requestFocus()

            val position = holder.adapterPosition
            if (position == RecyclerView.NO_POSITION) {
                Log.d(TAG, "Item no longer exists.")
            } else if (clickEnabled) {
                viewModel.onLongClick(position)
            }

            true
        }

        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position == RecyclerView.NO_POSITION) {
                Log.d(TAG, "Item no longer exists.")
            } else if (clickEnabled) {
                viewModel.onClickArea(position)
            }
        }

        return holder
    }

    private fun getItem(position: Int) = field[position]

    override fun getItemId(position: Int): Long = getItem(position).id.toLong()

    override fun onBindViewHolder(holder: FieldViewHolder, position: Int) {
        val field = getItem(position)
        holder.areaView.bindField(field, isAmbientMode, isLowBitAmbient, paintSettings)
    }

    companion object {
        private const val TAG = "AreaAdapter"

        private fun createAreaPaintSettings(context: Context, useLargeArea: Boolean): AreaPaintSettings {
            val resources = context.resources
            val padding = resources.getDimension(R.dimen.field_padding)
            val size = if (useLargeArea) {
                resources.getDimension(R.dimen.accessible_field_size).toInt()
            } else {
                resources.getDimension(R.dimen.field_size).toInt()
            }
            return AreaPaintSettings(
                Paint().apply {
                    isAntiAlias = true
                    isDither = true
                    style = Paint.Style.FILL
                    textSize = 18.0f * context.resources.displayMetrics.density
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                },
                RectF(padding, padding, size - padding, size - padding),
                resources.getDimension(R.dimen.field_radius)
            )
        }
    }
}
