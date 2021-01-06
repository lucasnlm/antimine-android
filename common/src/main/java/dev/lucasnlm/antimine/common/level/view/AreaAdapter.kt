package dev.lucasnlm.antimine.common.level.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.AreaPaintSettings
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.ui.view.AreaView
import dev.lucasnlm.antimine.ui.view.createAreaPaintSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AreaAdapter(
    context: Context,
    private val viewModel: GameViewModel,
    private val preferencesRepository: IPreferencesRepository,
    dimensionRepository: IDimensionRepository,
    private val coroutineScope: CoroutineScope,
) : RecyclerView.Adapter<AreaViewHolder>() {

    private var field = listOf<Area>()
    private var isLowBitAmbient = false
    private var isAmbientMode = false
    private val paintSettings: AreaPaintSettings

    private var clickEnabled: Boolean = false

    private val theme = viewModel.getAppTheme()

    init {
        setHasStableIds(true)
        paintSettings = createAreaPaintSettings(
            context.applicationContext,
            dimensionRepository.areaSize(),
            preferencesRepository.squareRadius(),
        )
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
                coroutineScope.launch {
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
                view.setOnDoubleClickListener(
                    object : GestureDetector.OnDoubleTapListener {
                        override fun onDoubleTap(e: MotionEvent?): Boolean {
                            return view.onClickablePosition(absoluteAdapterPosition) {
                                viewModel.onDoubleClick(it)
                            }
                        }

                        override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
                            return false
                        }

                        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                            return view.onClickablePosition(absoluteAdapterPosition) {
                                viewModel.onSingleClick(it)
                            }
                        }
                    }
                )
            } else {
                var longClickJob: Job? = null
                view.setOnTouchListener { _, motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            view.isPressed = true
                            longClickJob = coroutineScope.launch {
                                delay(preferencesRepository.customLongPressTimeout())
                                view.onClickablePosition(absoluteAdapterPosition) {
                                    viewModel.onLongClick(it)
                                }
                                longClickJob = null
                            }
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            view.isPressed = false
                            longClickJob?.let { job ->
                                job.cancel()
                                longClickJob = null
                                view.onClickablePosition(absoluteAdapterPosition) {
                                    viewModel.onSingleClick(it)
                                }
                            } ?: false
                        }
                        else -> false
                    }
                }
            }

            var longClickJob: Job? = null
            view.setOnKeyListener { _, keyCode, keyEvent ->
                var handled = false
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    when (keyEvent.action) {
                        KeyEvent.ACTION_DOWN -> {
                            handled = true
                            view.isPressed = true
                            longClickJob = coroutineScope.launch {
                                delay(preferencesRepository.customLongPressTimeout())
                                view.onClickablePosition(absoluteAdapterPosition) {
                                    viewModel.onLongClick(it)
                                }
                                longClickJob = null
                            }
                        }
                        KeyEvent.ACTION_UP -> {
                            handled = true
                            view.isPressed = false
                            longClickJob?.let { job ->
                                job.cancel()
                                longClickJob = null
                                view.onClickablePosition(absoluteAdapterPosition) {
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
                itemView.bindField(field, theme, isAmbientMode, isLowBitAmbient, paintSettings)
            }
        }
    }

    companion object {
        val TAG = AreaAdapter::class.simpleName!!
    }
}
