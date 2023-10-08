package dev.lucasnlm.antimine.licenses.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import dev.lucasnlm.antimine.about.R
import dev.lucasnlm.antimine.about.databinding.FragmentLicensesBinding
import dev.lucasnlm.antimine.licenses.viewmodel.LicenseViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LicensesFragment : Fragment(R.layout.fragment_licenses) {
    private lateinit var binding: FragmentLicensesBinding
    private val viewModel: LicenseViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLicensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel
                .observeState()
                .collect {
                    binding.licenses.apply {
                        setHasFixedSize(true)
                        addItemDecoration(
                            MaterialDividerItemDecoration(context, MaterialDividerItemDecoration.VERTICAL),
                        )
                        layoutManager = LinearLayoutManager(context)
                        adapter = LicensesAdapter(it.licenses)
                    }
                }
        }
    }
}
