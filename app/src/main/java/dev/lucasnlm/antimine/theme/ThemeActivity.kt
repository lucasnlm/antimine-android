package dev.lucasnlm.antimine.theme

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.ThematicActivity
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.view.SpaceItemDecoration
import dev.lucasnlm.antimine.support.SupportAppDialogFragment
import dev.lucasnlm.antimine.theme.view.ThemeAdapter
import dev.lucasnlm.antimine.theme.viewmodel.ThemeViewModel
import kotlinx.android.synthetic.main.activity_theme.*
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ThemeActivity : ThematicActivity(R.layout.activity_theme) {
    private val dimensionRepository: IDimensionRepository by inject()

    private val themeViewModel by viewModel<ThemeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenCreated {
            val gaps = resources.getDimension(R.dimen.theme_divider) * 6
            val areaSize = (dimensionRepository.displaySize().width - gaps) / 9f

            recyclerView.apply {
                addItemDecoration(SpaceItemDecoration(R.dimen.theme_divider))
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(context, 3)
                adapter = ThemeAdapter(themeViewModel, areaSize)
            }

            themeViewModel.observeState().collect {
                if (usingTheme.id != it.current.id) {
                    recreate()
                }
            }
        }
    }

    private fun showUnlockDialog() {
        if (supportFragmentManager.findFragmentByTag(SupportAppDialogFragment.TAG) == null) {
            SupportAppDialogFragment().apply {
                show(supportFragmentManager, SupportAppDialogFragment.TAG)
            }
        }
    }
}
