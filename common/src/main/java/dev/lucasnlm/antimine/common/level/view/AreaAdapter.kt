package dev.lucasnlm.antimine.common.level.view

import android.annotation.SuppressLint
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
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.control.ControlStyle
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AreaAdapter(
    context: Context,
    private val viewModel: GameViewModel,
    private val preferencesRepository: IPreferencesRepository,
    dimensionRepository: IDimensionRepository
) : RecyclerView.Adapter<AreaViewHolder>() {

    private var field = listOf<Area>()
    private var isLowBitAmbient = false
    private var isAmbientMode = false
    private val paintSettings: AreaPaintSettings

    private var clickEnabled: Boolean = false

    init {
        setHasStableIds(true)
        paintSettings = createAreaPaintSettings(context.applicationContext, dimensionRepository.areaSize())
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

    private fun AreaView.onClickablePosition(position: Int, action: suspend (Int) -> Unit): Boolean {
        return when {
            position == RecyclerView.NO_POSITION -> {
                Log.d(TAG, "Item no longer exists.")
                false
            }
            clickEnabled -> {
                requestFocus()
                GlobalScope.launch {
                    action(position)
                }
                true
            }
            else -> false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaViewHolder {
        val view = AreaView(parent.context)
        return AreaViewHolder(view).apply {
            val style = preferencesRepository.controlStyle()
            if (style == ControlStyle.DoubleClick || style == ControlStyle.DoubleClickInverted) {
                view.isClickable = true
                view.setOnDoubleClickListener(object : GestureDetector.OnDoubleTapListener {
                    override fun onDoubleTap(e: MotionEvent?): Boolean {
                        return view.onClickablePosition(adapterPosition) {
                            viewModel.onDoubleClick(it)
                        }
                    }

                    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
                        return false
                    }

                    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                        return view.onClickablePosition(adapterPosition) {
                            viewModel.onSingleClick(it)
                        }
                    }
                })
            } else {
                view.setOnTouchListener { _, motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            view.isPressed = true
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            view.isPressed = false
                            val dt = motionEvent.eventTime - motionEvent.downTime

                            if (dt > preferencesRepository.customLongPressTimeout()) {
                                view.onClickablePosition(adapterPosition) {
                                    viewModel.onLongClick(it)
                                }
                            } else {
                                view.onClickablePosition(adapterPosition) {
                                    viewModel.onSingleClick(it)
                                }
                            }
                        }
                        else -> false
                    }
                }
            }

            view.setOnKeyListener { _, keyCode, keyEvent ->
                var handled = false
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    when (keyEvent.action) {
                        KeyEvent.ACTION_DOWN -> {
                            handled = true
                            view.isPressed = true
                        }
                        KeyEvent.ACTION_UP -> {
                            handled = true
                            view.isPressed = false
                            val dt = keyEvent.eventTime - keyEvent.downTime
                            if (dt > preferencesRepository.customLongPressTimeout()) {
                                view.onClickablePosition(adapterPosition) {
                                    viewModel.onLongClick(it)
                                }
                            } else {
                                view.onClickablePosition(adapterPosition) {
                                    viewModel.onSingleClick(it)
                                }
                            }
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
                itemView.bindField(field, viewModel.getAppTheme(), isAmbientMode, isLowBitAmbient, paintSettings)
            }
        }
    }

    companion object {
        val TAG = AreaAdapter::class.simpleName!!

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
                resources.getDimension(R.dimen.field_radius)
            )
        }
    }
}
