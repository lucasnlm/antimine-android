package dev.lucasnlm.antimine.about.views.translators

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import kotlinx.android.synthetic.main.fragment_translators.*

class TranslatorsFragment : Fragment() {
    private var aboutViewModel: AboutViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.run {
            aboutViewModel = ViewModelProviders.of(this).get(AboutViewModel::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_translators, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        translators.apply {
            addItemDecoration(
                DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL)
            )
            layoutManager = LinearLayoutManager(view.context)
            adapter = aboutViewModel?.getTranslators()
        }
    }
}
