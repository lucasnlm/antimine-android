package dev.lucasnlm.antimine.control.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.control.R
import dev.lucasnlm.antimine.control.models.ControlDetails
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import kotlinx.android.synthetic.main.view_control_item.view.*

class ControlAdapter(
    private val themeRepository: IThemeRepository,
    private var selected: ControlStyle,
    private val controls: MutableList<ControlDetails>,
    private val onControlSelected: (ControlStyle) -> Unit,
) : RecyclerView.Adapter<ControlViewHolder>() {

    init {
        setHasStableIds(true)
    }

    fun bindControlStyleList(selected: ControlStyle, list: List<ControlDetails>) {
        controls.apply {
            clear()
            addAll(list)
        }
        this.selected = selected
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return controls[position].id
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 4) SIMPLE else COMPLEX
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ControlViewHolder {
        return if (viewType == SIMPLE) {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.view_control_item_simple, parent, false)
            ControlViewHolder(view)
        } else {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.view_control_item, parent, false)
            ControlViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ControlViewHolder, position: Int) {
        val controlDetail = controls[position]
        val isSelected = selected == controlDetail.controlStyle

        if (getItemViewType(position) == SIMPLE) {
            holder.itemView.run {
                cardView.apply {
                    setOnClickListener {
                        onControlSelected(controlDetail.controlStyle)
                    }
                    strokeColor = if (isSelected) {
                        themeRepository.getTheme().palette.accent.toAndroidColor()
                    } else {
                        0x000000.toAndroidColor(32)
                    }
                }
                firstActionName.text = context.getString(controlDetail.firstActionId)
            }
        } else {
            holder.itemView.run {
                cardView.apply {
                    setOnClickListener {
                        onControlSelected(controlDetail.controlStyle)
                    }
                    strokeColor = if (isSelected) {
                        themeRepository.getTheme().palette.accent.toAndroidColor()
                    } else {
                        0x000000.toAndroidColor(32)
                    }
                }
                firstActionName.text = context.getString(controlDetail.firstActionId)
                firstActionResponse.text = context.getString(controlDetail.firstActionResponseId)
                secondActionName.text = context.getString(controlDetail.secondActionId)
                secondActionResponse.text = context.getString(controlDetail.secondActionResponseId)
            }
        }
    }

    override fun getItemCount(): Int {
        return controls.size
    }

    companion object {
        private const val COMPLEX = 0
        private const val SIMPLE = 1
    }
}
