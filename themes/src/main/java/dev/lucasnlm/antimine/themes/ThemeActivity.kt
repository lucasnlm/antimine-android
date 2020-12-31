package dev.lucasnlm.antimine.themes

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.purchases.SupportAppDialogFragment
import dev.lucasnlm.antimine.themes.view.ThemeAdapter
import dev.lucasnlm.antimine.themes.viewmodel.ThemeEvent
import dev.lucasnlm.antimine.themes.viewmodel.ThemeViewModel
import dev.lucasnlm.antimine.ui.ThematicActivity
import dev.lucasnlm.antimine.ui.view.SpaceItemDecoration
import dev.lucasnlm.external.IBillingManager
import kotlinx.android.synthetic.main.activity_theme.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ThemeActivity : ThematicActivity(R.layout.activity_theme) {
    private val dimensionRepository: IDimensionRepository by inject()

    private val themeViewModel by viewModel<ThemeViewModel>()

    private val cloudSaveManager by inject<CloudSaveManager>()

    private val preferencesRepository: IPreferencesRepository by inject()

    private val billingManager: IBillingManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenCreated {
            val gaps = resources.getDimension(R.dimen.theme_divider) * 6
            val size = dimensionRepository.displaySize()
            val areaSize: Float
            val columns: Int

            if (size.width > size.height) {
                areaSize = (size.width - gaps) / 15f
                columns = 5
            } else {
                areaSize = (size.width - gaps) / 9f
                columns = 3
            }

            recyclerView.apply {
                addItemDecoration(SpaceItemDecoration(R.dimen.theme_divider))
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(context, columns)
                adapter = ThemeAdapter(themeViewModel, areaSize, preferencesRepository.squareRadius())
            }

            launch {
                themeViewModel.observeEvent().collect {
                    if (it is ThemeEvent.Unlock) {
                        showUnlockDialog(it.themeId)
                    }
                }
            }

            launch {
                themeViewModel.observeState().collect {
                    if (usingTheme.id != it.current.id) {
                        recreate()
                        cloudSaveManager.uploadSave()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (themeViewModel.singleState().current.id != 0L) {
            menuInflater.inflate(R.menu.delete_icon_menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.delete) {
            themeViewModel.sendEvent(ThemeEvent.ResetTheme)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun showUnlockDialog(themeId: Long) {
        if (supportFragmentManager.findFragmentByTag(SupportAppDialogFragment.TAG) == null) {
            lifecycleScope.launch {
                SupportAppDialogFragment.newChangeThemeDialog(
                    applicationContext,
                    themeId,
                    billingManager.getPrice().singleOrNull()
                ).apply {
                    show(supportFragmentManager, SupportAppDialogFragment.TAG)
                }
            }
        }
    }
}
