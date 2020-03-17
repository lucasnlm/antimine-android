package dev.lucasnlm.antimine.common.level.view

import android.content.Context
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.AreaPaintSettings
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel

class AreaAdapter(
    context: Context,
    private val viewModel: GameViewModel
) : RecyclerView.Adapter<AreaViewHolder>() {

    private var field = listOf<Area>()
    private var isLowBitAmbient = false
    private var isAmbientMode = false
    private val paintSettings: AreaPaintSettings

    private var clickEnabled: Boolean = false
    private var longPressAt: Long = 0L

    init {
        setHasStableIds(true)
        paintSettings = createAreaPaintSettings(context, viewModel.useAccessibilityMode())
    }

    fun setAmbientMode(isAmbientMode: Boolean, isLowBitAmbient: Boolean) {
        this.isLowBitAmbient = isLowBitAmbient
        this.isAmbientMode = isAmbientMode
    }

    fun setClickEnabled(value: Boolean) {
        this.clickEnabled = value
    }

    fun bindField(area: List<Area>) {
        this.field = area
        notifyDataSetChanged()
    }

    override fun getItemCount() = field.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaViewHolder {
        val layout = if (viewModel.useAccessibilityMode()) {
            R.layout.view_accessibility_field
        } else {
            R.layout.view_field
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        val holder = AreaViewHolder(view)

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

        holder.areaView.setOnKeyListener { _, keyCode, keyEvent ->
            var handled = false

            if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                when (keyEvent.action) {
                    KeyEvent.ACTION_DOWN -> {
                        longPressAt = System.currentTimeMillis()
                        handled = true
                    }
                    KeyEvent.ACTION_UP -> {
                        if (clickEnabled) {
                            val value = System.currentTimeMillis() - longPressAt
                            if (value > 300L) {
                                view.performLongClick()
                            } else {
                                view.callOnClick()
                            }
                        }
                        longPressAt = System.currentTimeMillis()
                        handled = true
                    }
                }
            }

            handled
        }

        return holder
    }

    private fun getItem(position: Int) = field[position]

    override fun getItemId(position: Int): Long = getItem(position).id.toLong()

    override fun onBindViewHolder(holder: AreaViewHolder, position: Int) {
        val field = getItem(position)
        holder.areaView.bindField(field, isAmbientMode, isLowBitAmbient, paintSettings)
    }

    companion object {
        private val TAG = AreaAdapter::class.simpleName

        fun createAreaPaintSettings(context: Context, useLargeArea: Boolean): AreaPaintSettings {
            val resources = context.resources
            val size = if (useLargeArea) {
                resources.getDimension(R.dimen.accessible_field_size)
            } else {
                resources.getDimension(R.dimen.field_size)
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
                RectF(0.0f, 0.0f, size, size),
                resources.getDimension(R.dimen.field_radius)
            )
        }
    }
}
