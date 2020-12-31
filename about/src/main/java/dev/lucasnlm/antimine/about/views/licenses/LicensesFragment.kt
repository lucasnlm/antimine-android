package dev.lucasnlm.antimine.about.views.licenses

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dev.lucasnlm.antimine.about.R
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import kotlinx.android.synthetic.main.fragment_licenses.*
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LicensesFragment : Fragment(R.layout.fragment_licenses) {
    private val aboutViewModel: AboutViewModel by sharedViewModel()

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
