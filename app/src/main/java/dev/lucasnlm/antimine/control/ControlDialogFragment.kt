package dev.lucasnlm.antimine.control

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.control.view.ControlItemView
import dev.lucasnlm.antimine.control.viewmodel.ControlEvent
import dev.lucasnlm.antimine.control.viewmodel.ControlViewModel

@AndroidEntryPoint
class ControlDialogFragment : AppCompatDialogFragment() {
    private val controlViewModel by activityViewModels<ControlViewModel>()
    private val adapter by lazy { ControlListAdapter(controlViewModel) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val currentControl = controlViewModel.singleState().selectedId
        return AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.control)
            setSingleChoiceItems(adapter, currentControl, null)
            setPositiveButton(R.string.ok, null)
        }.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (activity is DialogInterface.OnDismissListener) {
            (activity as DialogInterface.OnDismissListener).onDismiss(dialog)
        }
        super.onDismiss(dialog)
    }

    private class ControlListAdapter(
        private val controlViewModel: ControlViewModel
    ) : BaseAdapter() {
        private val controlList = controlViewModel.singleState().gameControls

        fun getSelectedId() = controlViewModel.singleState().selectedId

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = if (convertView == null) {
                ControlItemView(parent!!.context)
            } else {
                (convertView as ControlItemView)
            }

            val selectedId = getSelectedId()

            return view.apply {
                val controlModel = controlList[position]
                bind(controlModel)
                setRadio(selectedId == controlModel.controlStyle.ordinal)
                setOnClickListener {
                    controlViewModel.sendEvent(ControlEvent.SelectControlStyle(controlModel.controlStyle))
                    notifyDataSetChanged()
                }
            }
        }

        override fun hasStableIds(): Boolean = true

        override fun getItem(position: Int): Any = controlList[position]

        override fun getItemId(position: Int): Long = controlList[position].id

        override fun getCount(): Int = controlList.count()
    }

    companion object {
        val TAG = ControlDialogFragment::class.simpleName!!
    }
}
