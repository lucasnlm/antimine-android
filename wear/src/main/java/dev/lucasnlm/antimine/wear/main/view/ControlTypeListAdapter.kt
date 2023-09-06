package dev.lucasnlm.antimine.wear.main.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.wear.R
import dev.lucasnlm.antimine.wear.databinding.ViewControlTypeItemBinding
import dev.lucasnlm.antimine.wear.main.models.ControlTypeItem

class ControlTypeListAdapter(
    private val controlTypeItemList: List<ControlTypeItem>,
    private val onChangeControl: () -> Unit,
    private val preferencesRepository: PreferencesRepository,
) : RecyclerView.Adapter<ControlTypeListAdapter.RecyclerViewHolder>() {
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder {
        val binding = ViewControlTypeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecyclerViewHolder(
            binding = binding,
            preferencesRepository = preferencesRepository,
            onChangeControl = onChangeControl,
        )
    }

    override fun getItemCount(): Int {
        return controlTypeItemList.size
    }

    override fun getItemId(position: Int): Long {
        return controlTypeItemList[position].id
    }

    override fun onBindViewHolder(
        holder: RecyclerViewHolder,
        position: Int,
    ) {
        val menuItem = controlTypeItemList[position]
        holder.bind(menuItem)
    }

    class RecyclerViewHolder(
        private val binding: ViewControlTypeItemBinding,
        private val preferencesRepository: PreferencesRepository,
        private val onChangeControl: () -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(controlTypeItem: ControlTypeItem) {
            val context = binding.root.context
            val isSelected = controlTypeItem.controlStyle == preferencesRepository.controlStyle()

            binding.apply {
                val imageRes =
                    if (isSelected) {
                        R.drawable.radio_button_checked
                    } else {
                        R.drawable.radio_button_unchecked
                    }
                radio.setImageResource(imageRes)

                firstActionLabel.text = context.getString(controlTypeItem.primaryAction)

                val hasSecondaryAction = controlTypeItem.secondaryAction != null

                if (hasSecondaryAction) {
                    secondActionLabel.text = controlTypeItem.secondaryAction?.let { context.getString(it) }
                }

                secondActionLabel.isVisible = hasSecondaryAction
                flag.isVisible = hasSecondaryAction
                shovel.isVisible = hasSecondaryAction

                root.setOnClickListener {
                    preferencesRepository.useControlStyle(controlTypeItem.controlStyle)
                    onChangeControl()
                }
            }
        }
    }
}
