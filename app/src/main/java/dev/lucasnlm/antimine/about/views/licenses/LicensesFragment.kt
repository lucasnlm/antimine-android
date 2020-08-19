package dev.lucasnlm.antimine.about.views.licenses

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import kotlinx.android.synthetic.main.fragment_licenses.*
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject

class LicensesFragment : Fragment(R.layout.fragment_licenses) {
    private val aboutViewModel: AboutViewModel by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenResumed {
            aboutViewModel
                .observeState()
                .collect {
                    activity?.setTitle(R.string.licenses)

                    licenses.apply {
                        setHasFixedSize(true)
                        addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                        layoutManager = LinearLayoutManager(context)
                        adapter = LicensesAdapter(it.licenses)
                    }
                }
        }
    }

    companion object {
        val TAG = LicensesFragment::class.simpleName!!
    }
}
