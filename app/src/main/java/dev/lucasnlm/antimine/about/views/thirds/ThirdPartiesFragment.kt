package dev.lucasnlm.antimine.about.views.thirds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import kotlinx.android.synthetic.main.fragment_third_party.*

class ThirdPartiesFragment : Fragment(R.layout.fragment_third_party) {
    private val aboutViewModel: AboutViewModel by activityViewModels()

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.licenses)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        licenses.apply {
            setHasFixedSize(true)
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            )
            layoutManager = LinearLayoutManager(context)
        }

        licenses.adapter = aboutViewModel.getLicenses()
    }

    companion object {
        val TAG = ThirdPartiesFragment::class.simpleName!!
    }
}
