package dev.lucasnlm.antimine.themes

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.themes.view.SkinAdapter
import dev.lucasnlm.antimine.themes.view.ThemeAdapter
import dev.lucasnlm.antimine.themes.viewmodel.ThemeEvent
import dev.lucasnlm.antimine.themes.viewmodel.ThemeViewModel
import dev.lucasnlm.antimine.ui.ext.ThematicActivity
import dev.lucasnlm.antimine.ui.view.SpaceItemDecoration
import dev.lucasnlm.external.IAdsManager
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.android.synthetic.main.activity_theme.*
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
    private val analyticsManager: IAnalyticsManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analyticsManager.sentEvent(Analytics.OpenThemes)

        if (!preferencesRepository.isPremiumEnabled()) {
            adsManager.start(this)
        }

        bindToolbar(toolbar)

        if (preferencesRepository.isPremiumEnabled()) {
            unlockAll.visibility = View.GONE
        } else {
            unlockAll.bind(
                theme = usingTheme,
                invert = true,
                text = getString(R.string.remove_ad),
                onAction = {
                    lifecycleScope.launch {
                        billingManager.charge(this@ThemeActivity)
                    }
                },
            )

            lifecycleScope.launchWhenResumed {
                billingManager.getPriceFlow().collect {
                    unlockAll.bind(
                        theme = usingTheme,
                        invert = true,
                        text = getString(R.string.unlock),
                        price = it.price,
                        showOffer = it.offer,
                        onAction = {
                            lifecycleScope.launch {
                                billingManager.charge(this@ThemeActivity)
                            }
                        },
                    )
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            val size = dimensionRepository.displaySize()
            val columns = if (size.width > size.height) { 5 } else { 3 }

            val themeAdapter = ThemeAdapter(
                themeViewModel = themeViewModel,
                preferencesRepository = preferencesRepository,
                onSelectTheme = { theme ->
                    themeViewModel.sendEvent(ThemeEvent.ChangeTheme(theme))
                },
                onRequestPurchase = {
                    lifecycleScope.launch {
                        billingManager.charge(this@ThemeActivity)
                    }
                },
            )

            val skinAdapter = SkinAdapter(
                themeViewModel = themeViewModel,
                preferencesRepository = preferencesRepository,
                onSelectSkin = { skin ->
                    themeViewModel.sendEvent(ThemeEvent.ChangeSkin(skin))
                },
                onRequestPurchase = {
                    lifecycleScope.launch {
                        billingManager.charge(this@ThemeActivity)
                    }
                },
            )

            themes.apply {
                addItemDecoration(SpaceItemDecoration(R.dimen.theme_divider))
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(context, columns)
                adapter = themeAdapter
            }

            skins.apply {
                addItemDecoration(SpaceItemDecoration(R.dimen.theme_divider))
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                adapter = skinAdapter
            }

            if (!preferencesRepository.isPremiumEnabled()) {
                lifecycleScope.launchWhenResumed {
                    billingManager.listenPurchases().collect {
                        if (it is PurchaseInfo.PurchaseResult && it.unlockStatus) {
                            themeAdapter.notifyItemRangeChanged(0, themeAdapter.itemCount)
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
                    if (usingTheme != it.currentTheme || usingSkin != it.currentAppSkin) {
                        recreate()
                        cloudSaveManager.uploadSave()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(SCROLL_VIEW_STATE, intArrayOf(scrollView.scrollX, scrollView.scrollY))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getIntArray(SCROLL_VIEW_STATE)?.let { position ->
            scrollView.post { scrollView.scrollTo(position[0], position[1]) }
        }
    }

    companion object {
        const val SCROLL_VIEW_STATE = "SCROLL_VIEW_POSITION"
    }
}
