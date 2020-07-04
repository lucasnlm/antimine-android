package dev.lucasnlm.antimine.common.level.view

import android.content.Context
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.AreaPaintSettings
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.control.ControlStyle
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository

class AreaAdapter(
    context: Context,
    private val viewModel: GameViewModel,
    private val preferencesRepository: IPreferencesRepository
) : RecyclerView.Adapter<AreaViewHolder>() {

    private var field = listOf<Area>()
    private var isLowBitAmbient = false
    private var isAmbientMode = false
    private val paintSettings: AreaPaintSettings

    private var clickEnabled: Boolean = false
    private var longPressAt: Long = 0L

    init {
        setHasStableIds(true)
        paintSettings = createAreaPaintSettings(context.applicationContext, viewModel.useAccessibilityMode())
    }

    fun setAmbientMode(isAmbientMode: Boolean, isLowBitAmbient: Boolean) {
        this.isLowBitAmbient = isLowBitAmbient
        this.isAmbientMode = isAmbientMode
    }

    fun setClickEnabled(value: Boolean) {
        clickEnabled = value
    }

    fun bindField(field: List<Area>) {
        this.field = field
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = field.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaViewHolder {
        val view = AreaView(parent.context)
        return AreaViewHolder(view).apply {
            view.setOnDoubleClickListener(object : GestureDetector.OnDoubleTapListener {
                override fun onDoubleTap(e: MotionEvent?): Boolean {
                    return false
                }

                override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
                    val position = adapterPosition
                    return when {
                        position == RecyclerView.NO_POSITION -> {
                            Log.d(TAG, "Item no longer exists.")
                            false
                        }
                        clickEnabled -> {
                            viewModel.onDoubleClickArea(position)
                            true
                        }
                        else -> {
                            false
                        }
                    }
                }

                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                    if (preferencesRepository.controlStyle() == ControlStyle.DoubleClick) {
                        val position = adapterPosition
                        if (position == RecyclerView.NO_POSITION) {
                            Log.d(TAG, "Item no longer exists.")
                        } else if (clickEnabled) {
                            viewModel.onSingleClick(position)
                            return true
                        }
                    }
                    return false
                }
            })

            itemView.setOnLongClickListener { target ->
                target.requestFocus()

                val position = adapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    Log.d(TAG, "Item no longer exists.")
                } else if (clickEnabled) {
                    viewModel.onLongClick(position)
                }

                true
            }

            itemView.setOnClickListener {
                if (preferencesRepository.controlStyle() != ControlStyle.DoubleClick) {
                    val position = adapterPosition
                    if (position == RecyclerView.NO_POSITION) {
                        Log.d(TAG, "Item no longer exists.")
                    } else if (clickEnabled) {
                        viewModel.onSingleClick(position)
                    }
                }
            }

            itemView.setOnKeyListener { _, keyCode, keyEvent ->
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
        }
    }

    private fun getItem(position: Int) = field[position]

    override fun getItemId(position: Int): Long = getItem(position).id.toLong()

    override fun onBindViewHolder(holder: AreaViewHolder, position: Int) {
        val field = getItem(position)
        holder.run {
            if (itemView is AreaView) {
                itemView.bindField(field, isAmbientMode, isLowBitAmbient, paintSettings)
            }
        }
    }

    companion object {
        val TAG = AreaAdapter::class.simpleName!!

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
