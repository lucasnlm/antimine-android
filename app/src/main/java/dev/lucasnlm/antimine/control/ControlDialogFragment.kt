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
import dev.lucasnlm.antimine.control.model.ControlModel
import dev.lucasnlm.antimine.control.view.ControlItemView
import dev.lucasnlm.antimine.control.viewmodel.ControlViewModel
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import javax.inject.Inject

@AndroidEntryPoint
class ControlDialogFragment : AppCompatDialogFragment() {
    @Inject
    lateinit var preferencesRepository: IPreferencesRepository

    private val controlViewModel by activityViewModels<ControlViewModel>()
    private val adapter by lazy { ControlListAdapter(controlViewModel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter.setList(controlViewModel.gameControlOptions)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val currentControl = preferencesRepository.controlType().ordinal

        return AlertDialog.Builder(requireContext(), R.style.MyDialog).apply {
            setTitle(R.string.control)
            setSingleChoiceItems(adapter, currentControl, null)
            setPositiveButton(R.string.ok, null) // TODO OK
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
        private var selected = controlViewModel.controlTypeSelected.value
        private var controlList = listOf<ControlModel>()

        fun setList(list: List<ControlModel>) {
            controlList = list
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = if (convertView == null) {
                ControlItemView(parent!!.context)
            } else {
                (convertView as ControlItemView)
            }

            return view.apply {
                val controlModel = controlList[position]
                bind(controlModel)
                setRadio(selected == controlModel.controlStyle)
                setOnClickListener {
                    controlViewModel.selectControlType(controlModel.controlStyle)
                    selected = controlModel.controlStyle
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
