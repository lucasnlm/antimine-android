package dev.lucasnlm.antimine.themes

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.slider.Slider
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.themes.view.ThemeAdapter
import dev.lucasnlm.antimine.themes.viewmodel.ThemeEvent
import dev.lucasnlm.antimine.themes.viewmodel.ThemeViewModel
import dev.lucasnlm.antimine.ui.ext.ThematicActivity
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.antimine.ui.view.SpaceItemDecoration
import dev.lucasnlm.external.IAdsManager
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.android.synthetic.main.activity_theme.*
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ThemeActivity : ThematicActivity(R.layout.activity_theme), Slider.OnChangeListener {
    private val themeViewModel by viewModel<ThemeViewModel>()

    private val themeRepository: IThemeRepository by inject()
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

        bindToolbar()

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
                        text = getString(R.string.remove_ad),
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

        squareSize.addOnChangeListener(this)
        squareDivider.addOnChangeListener(this)
        squareRadius.addOnChangeListener(this)

        lifecycleScope.launchWhenCreated {
            val size = dimensionRepository.displaySize()
            val columns = if (size.width > size.height) { 5 } else { 3 }

            val themeAdapter = ThemeAdapter(
                themeRepository = themeRepository,
                activity = this@ThemeActivity,
                themeViewModel = themeViewModel,
                preferencesRepository = preferencesRepository,
                adsManager = adsManager,
            )

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

            refreshForm()
        }
    }

    private fun refreshForm() {
        val state = themeViewModel.singleState()
        squareSize.value = (state.squareSize / squareSize.stepSize).toInt() * squareSize.stepSize
        squareDivider.value = (state.squareDivider / squareDivider.stepSize).toInt() * squareDivider.stepSize
        squareRadius.value = (state.squareRadius / squareRadius.stepSize).toInt() * squareRadius.stepSize
    }

    private fun bindToolbar() {
        section.bind(
            text = R.string.themes,
            startButton = R.drawable.back_arrow,
            startDescription = R.string.back,
            startAction = {
                finish()
            },
            endButton = R.drawable.undo,
            endDescription = R.string.delete_all,
            endAction = {
                themeViewModel.sendEvent(ThemeEvent.ResetTheme)
                lifecycleScope.launch {
                    refreshForm()
                }
                bindToolbar()
                section.showEndAction(false)
            },
        )
    }

    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        if (fromUser) {
            val progress = value.toInt()
            when (slider) {
                squareSize -> {
                    themeViewModel.sendEvent(ThemeEvent.SetSquareSize(progress))
                }
                squareDivider -> {
                    themeViewModel.sendEvent(ThemeEvent.SetSquareDivider(progress))
                }
                squareRadius -> {
                    themeViewModel.sendEvent(ThemeEvent.SetSquareRadius(progress))
                }
            }
            section.showEndAction(true)
        }
    }
}
