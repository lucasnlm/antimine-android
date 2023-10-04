package dev.lucasnlm.antimine.control.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import dev.lucasnlm.antimine.control.databinding.ViewControlItemBinding
import dev.lucasnlm.antimine.control.databinding.ViewControlItemSimpleBinding
import dev.lucasnlm.antimine.control.models.ControlDetails
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import com.google.android.material.R as GR

class ControlAdapter(
    private var selected: ControlStyle,
    private val controls: MutableList<ControlDetails>,
    private val onControlSelected: (ControlStyle) -> Unit,
) : RecyclerView.Adapter<ControlViewHolder>() {

    init {
        setHasStableIds(true)
    }

    fun bindControlStyleList(
        selected: ControlStyle,
        list: List<ControlDetails>,
    ) {
        controls.apply {
            clear()
            addAll(list)
        }
        this.selected = selected
        notifyItemRangeChanged(0, controls.size)
    }

    override fun getItemId(position: Int): Long {
        return controls[position].id
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == SINGLE_LINE_ID) SINGLE_LINE_CONTROL else TWO_LINES_CONTROL
    }

    private fun <T> Int.inflateIf(
        type: Int,
        action: () -> T,
    ): T? {
        return if (this == type) {
            action()
        } else {
            null
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ControlViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return ControlViewHolder(
            simpleItem =
                viewType.inflateIf(SINGLE_LINE_CONTROL) {
                    ViewControlItemSimpleBinding.inflate(layoutInflater, parent, false)
                },
            controlItem =
                viewType.inflateIf(TWO_LINES_CONTROL) {
                    ViewControlItemBinding.inflate(layoutInflater, parent, false)
                },
        )
    }

    override fun onBindViewHolder(
        holder: ControlViewHolder,
        position: Int,
    ) {
        val controlDetail = controls[position]
        val isSelected = selected == controlDetail.controlStyle
        val context = holder.itemView.context

        val selectedBackgroundColor =
            MaterialColors.getColorStateListOrNull(
                context,
                GR.attr.colorOnBackground,
            )?.withAlpha(BACKGROUND_SELECTED_ALPHA)

        if (holder.simpleItem != null) {
            holder.itemView.run {
                holder.simpleItem.cardView.apply {
                    isSoundEffectsEnabled = false
                    setOnClickListener {
                        onControlSelected(controlDetail.controlStyle)
                    }
                    backgroundTintList =
                        if (isSelected) {
                            selectedBackgroundColor
                        } else {
                            null
                        }
                }
                holder.simpleItem.firstActionName.text = context.getString(controlDetail.firstActionId)
            }
        } else if (holder.controlItem != null) {
            holder.itemView.run {
                holder.controlItem.cardView.apply {
                    isSoundEffectsEnabled = false
                    setOnClickListener {
                        onControlSelected(controlDetail.controlStyle)
                    }
                    backgroundTintList =
                        if (isSelected) {
                            selectedBackgroundColor
                        } else {
                            null
                        }
                }
                holder.controlItem.firstActionName.text = context.getString(controlDetail.firstActionId)
                holder.controlItem.firstActionResponse.text = context.getString(controlDetail.firstActionResponseId)
                holder.controlItem.secondActionName.text = context.getString(controlDetail.secondActionId)
                holder.controlItem.secondActionResponse.text = context.getString(controlDetail.secondActionResponseId)
            }
        }
    }

    override fun getItemCount(): Int {
        return controls.size
    }

    companion object {
        private const val TWO_LINES_CONTROL = 0
        private const val SINGLE_LINE_CONTROL = 1
        private const val SINGLE_LINE_ID = 4
        private const val BACKGROUND_SELECTED_ALPHA = 50
    }
}
