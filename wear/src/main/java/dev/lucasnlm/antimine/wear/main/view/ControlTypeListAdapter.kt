package dev.lucasnlm.antimine.wear.main.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.wear.R
import dev.lucasnlm.antimine.wear.databinding.ViewControlTypeItemBinding
import dev.lucasnlm.antimine.wear.main.models.ControlTypeItem

class ControlTypeListAdapter(
    private val controlTypeItemList: List<ControlTypeItem>,
) : RecyclerView.Adapter<ControlTypeListAdapter.RecyclerViewHolder>() {
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val binding = ViewControlTypeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecyclerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return controlTypeItemList.size
    }

    override fun getItemId(position: Int): Long {
        return controlTypeItemList[position].id
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val menuItem = controlTypeItemList[position]
        holder.bind(menuItem)
    }

    class RecyclerViewHolder(
        private val binding: ViewControlTypeItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(controlTypeItem: ControlTypeItem) {
            val context = binding.root.context

            binding.apply {
                val imageRes = if (controlTypeItem.selected) {
                    R.drawable.radio_button_checked
                } else {
                    R.drawable.radio_button_unchecked
                }
                radio.setImageResource(imageRes)

                val actionStr: String = if (controlTypeItem.secondaryAction == null) {
                    context.getString(controlTypeItem.primaryAction)
                } else {
                    val firstAction = context.getString(controlTypeItem.primaryAction)
                    val secondAction = context.getString(controlTypeItem.secondaryAction)
                    "\t\uD83D\uDCA3 $firstAction, \uD83D\uDEA9 $secondAction"
                }

                action.text = actionStr

                root.setOnClickListener {
                    controlTypeItem.onClick()
                }
            }
        }
    }
}
