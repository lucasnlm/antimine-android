package dev.lucasnlm.antimine.about.views.translators

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.about.R
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import kotlinx.android.synthetic.main.fragment_translators.*
import kotlinx.android.synthetic.main.view_translator.view.*
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

internal class TranslatorsFragment : Fragment(R.layout.fragment_translators) {
    private val aboutViewModel: AboutViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenResumed {
            activity?.setTitle(R.string.translation)

            aboutViewModel.observeState().collect {
                translators.apply {
                    addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
                    layoutManager = LinearLayoutManager(view.context)
                    adapter = TranslatorsAdapter(it.translators)
                }
            }
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
