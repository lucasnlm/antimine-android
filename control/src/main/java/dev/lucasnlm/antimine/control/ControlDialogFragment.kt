package dev.lucasnlm.antimine.control

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import dev.lucasnlm.antimine.control.view.ControlItemView
import dev.lucasnlm.antimine.control.view.SimpleControlItemView
import dev.lucasnlm.antimine.control.viewmodel.ControlEvent
import dev.lucasnlm.antimine.control.viewmodel.ControlViewModel
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ControlDialogFragment : AppCompatDialogFragment() {
    private val cloudSaveManager by inject<CloudSaveManager>()
    private val controlViewModel by viewModel<ControlViewModel>()
    private val adapter by lazy { ControlListAdapter(controlViewModel) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val state = controlViewModel.singleState()
        return AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.control)
            setSingleChoiceItems(adapter, state.selectedIndex, null)
            setPositiveButton(R.string.ok, null)
        }.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (activity is DialogInterface.OnDismissListener) {
            (activity as DialogInterface.OnDismissListener).onDismiss(dialog)
        }
        cloudSaveManager.uploadSave()
        super.onDismiss(dialog)
    }

    private class ControlListAdapter(
        private val controlViewModel: ControlViewModel
    ) : BaseAdapter() {
        private val controlList = controlViewModel.singleState().gameControls

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            if (getItemViewType(position) == USE_COMMON_CONTROL_TYPE) {
                val view = if (convertView == null) {
                    ControlItemView(parent!!.context)
                } else {
                    (convertView as ControlItemView)
                }

                val selected = controlViewModel.singleState().selected

                return view.apply {
                    val controlModel = controlList[position]
                    bind(controlModel)
                    setRadio(selected == controlModel.controlStyle)
                    setOnClickListener {
                        controlViewModel.sendEvent(ControlEvent.SelectControlStyle(controlModel.controlStyle))
                        notifyDataSetChanged()
                    }
                }
            } else {
                val view = if (convertView == null) {
                    SimpleControlItemView(parent!!.context)
                } else {
                    (convertView as SimpleControlItemView)
                }

                val selected = controlViewModel.singleState().selected

                return view.apply {
                    val controlModel = controlList[position]
                    bind(controlModel)
                    setRadio(selected == controlModel.controlStyle)
                    setOnClickListener {
                        controlViewModel.sendEvent(ControlEvent.SelectControlStyle(controlModel.controlStyle))
                        notifyDataSetChanged()
                    }
                }
            }
        }

        override fun hasStableIds(): Boolean = true

        override fun getItem(position: Int): Any = controlList[position]

        override fun getItemId(position: Int): Long = controlList[position].id

        override fun getCount(): Int = controlList.count()

        override fun getItemViewType(position: Int): Int {
            return if (controlList[position].controlStyle == ControlStyle.SwitchMarkOpen) {
                USE_SIMPLE_CONTROL_TYPE
            } else {
                USE_COMMON_CONTROL_TYPE
            }
        }

        override fun getViewTypeCount(): Int = 2
    }

    companion object {
        val TAG = ControlDialogFragment::class.simpleName!!
        private const val USE_COMMON_CONTROL_TYPE = 1
        private const val USE_SIMPLE_CONTROL_TYPE = 0
    }
}
