package dev.lucasnlm.antimine.l10n

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.databinding.ActivityLocalizationBinding
import dev.lucasnlm.antimine.l10n.viewmodel.LocalizationEvent
import dev.lucasnlm.antimine.l10n.viewmodel.LocalizationViewModel
import dev.lucasnlm.antimine.l10n.views.LocalizationItemAdapter
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LocalizationActivity : ThemedActivity() {
    private val localizationViewModel by viewModel<LocalizationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLocalizationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bindToolbar(binding.toolbar)

        lifecycleScope.launch {
            localizationViewModel.observeState().collect { state ->
                if (!state.loading) {
                    binding.content.apply {
                        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                        adapter = LocalizationItemAdapter(
                            gameLanguages = state.languages,
                            onSelectLanguage = {
                                localizationViewModel.sendEvent(
                                    LocalizationEvent.SetLanguage(it),
                                )
                            },
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            localizationViewModel.observeSideEffects().collect {
                if (it is LocalizationEvent.FinishActivity) {
                    finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            localizationViewModel.sendEvent(LocalizationEvent.LoadAllLanguages)
        }
    }
}
