package dev.lucasnlm.antimine.about.views.translators

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import kotlinx.android.synthetic.main.fragment_translators.*
import kotlinx.android.synthetic.main.view_translator.view.*

internal class TranslatorsFragment : Fragment(R.layout.fragment_translators) {
    private val aboutViewModel: AboutViewModel by activityViewModels()

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.translation)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        translators.apply {
            addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(view.context)
            adapter = TranslatorsAdapter(aboutViewModel.getTranslatorsList())
        }
    }

    companion object {
        val TAG = TranslatorsFragment::class.simpleName!!
    }
}

class TranslatorsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val language: TextView = view.language
    val translators: TextView = view.translators
}
