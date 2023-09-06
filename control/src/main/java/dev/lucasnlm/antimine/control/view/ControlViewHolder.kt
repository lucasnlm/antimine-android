package dev.lucasnlm.antimine.control.view

import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.control.databinding.ViewControlItemBinding
import dev.lucasnlm.antimine.control.databinding.ViewControlItemSimpleBinding

class ControlViewHolder(
    val controlItem: ViewControlItemBinding?,
    val simpleItem: ViewControlItemSimpleBinding?,
) : RecyclerView.ViewHolder(
        (controlItem?.root ?: simpleItem?.root)!!,
    )
