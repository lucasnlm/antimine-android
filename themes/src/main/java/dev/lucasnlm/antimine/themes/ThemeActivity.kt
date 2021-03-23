package dev.lucasnlm.antimine.themes

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.themes.view.ThemeAdapter
import dev.lucasnlm.antimine.themes.viewmodel.ThemeEvent
import dev.lucasnlm.antimine.themes.viewmodel.ThemeViewModel
import dev.lucasnlm.antimine.ui.ThematicActivity
import dev.lucasnlm.antimine.ui.view.SpaceItemDecoration
import dev.lucasnlm.external.IAdsManager
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.android.synthetic.main.activity_theme.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ThemeActivity : ThematicActivity(R.layout.activity_theme) {
    private val themeViewModel by viewModel<ThemeViewModel>()

    private val dimensionRepository: IDimensionRepository by inject()
    private val cloudSaveManager by inject<CloudSaveManager>()
    private val preferencesRepository: IPreferencesRepository by inject()
    private val billingManager: IBillingManager by inject()
    private val adsManager: IAdsManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!preferencesRepository.isPremiumEnabled()) {
            adsManager.start(this)
        }

        bindToolbar(themeViewModel.singleState().current.id != 0L)

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

            val themeAdapter = ThemeAdapter(this@ThemeActivity, themeViewModel, areaSize, preferencesRepository, adsManager)

            recyclerView.apply {
                addItemDecoration(SpaceItemDecoration(R.dimen.theme_divider))
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(context, columns)
                adapter = themeAdapter
            }

            if (!preferencesRepository.isPremiumEnabled()) {
                lifecycleScope.launchWhenResumed {
                    billingManager.listenPurchases().collect {
                        if (it is PurchaseInfo.PurchaseResult && it.unlockStatus) {
                            themeAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            launch {
                themeViewModel.observeEvent().collect {
                    if (it is ThemeEvent.Unlock) {
                        billingManager.charge(this@ThemeActivity)
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

    private fun bindToolbar(hasDefinedTheme: Boolean) {
        if (hasDefinedTheme) {
            section.bind(
                text = R.string.themes,
                startButton = R.drawable.back_arrow,
                startDescription = R.string.back,
                startAction = {
                    finish()
                },
                endButton = R.drawable.delete,
                endDescription = R.string.delete_all,
                endAction = {
                    themeViewModel.sendEvent(ThemeEvent.ResetTheme)
                    bindToolbar(false)
                }
            )
        } else {
            section.bind(
                text = R.string.themes,
                startButton = R.drawable.back_arrow,
                startDescription = R.string.back,
                startAction = {
                    finish()
                }
            )
        }
    }
}
