package dev.lucasnlm.antimine.theme

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.ThematicActivity
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.view.SpaceItemDecoration
import dev.lucasnlm.antimine.theme.view.ThemeAdapter
import dev.lucasnlm.antimine.theme.viewmodel.ThemeViewModel
import kotlinx.android.synthetic.main.activity_theme.*
import javax.inject.Inject

@AndroidEntryPoint
class ThemeActivity : ThematicActivity(R.layout.activity_theme) {
    @Inject
    lateinit var dimensionRepository: IDimensionRepository

    private val viewModel by viewModels<ThemeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenCreated {
            val gaps = resources.getDimension(R.dimen.theme_divider) * 6
            val areaSize = (dimensionRepository.displaySize().width - gaps) / 9f

            recyclerView.apply {
                addItemDecoration(SpaceItemDecoration(R.dimen.theme_divider))
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(context, 3)
                adapter = ThemeAdapter(viewModel, areaSize)
            }

            viewModel.theme.observe(this@ThemeActivity, Observer {
                if (usingThemeId != it.id) {
                    recreate()
                }
            })
        }
    }
}
