package dev.lucasnlm.antimine.themes

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.themes.databinding.ActivityThemeBinding
import dev.lucasnlm.antimine.themes.view.SkinAdapter
import dev.lucasnlm.antimine.themes.view.ThemeAdapter
import dev.lucasnlm.antimine.themes.viewmodel.ThemeEvent
import dev.lucasnlm.antimine.themes.viewmodel.ThemeViewModel
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.ui.view.SpaceItemDecoration
import dev.lucasnlm.external.AdsManager
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.BillingManager
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import dev.lucasnlm.antimine.i18n.R as i18n

class ThemeActivity : ThemedActivity() {
    private val themeViewModel by viewModel<ThemeViewModel>()
    private val dimensionRepository: DimensionRepository by inject()
    private val cloudSaveManager by inject<CloudSaveManager>()
    private val preferencesRepository: PreferencesRepository by inject()
    private val billingManager: BillingManager by inject()
    private val adsManager: AdsManager by inject()
    private val analyticsManager: AnalyticsManager by inject()
    private val gameAudioManager: GameAudioManager by inject()

    private val binding: ActivityThemeBinding by lazy {
        ActivityThemeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        analyticsManager.sentEvent(Analytics.OpenThemes)

        if (!preferencesRepository.isPremiumEnabled()) {
            adsManager.start(this)
        }

        bindToolbar(binding.toolbar)

        if (preferencesRepository.isPremiumEnabled()) {
            binding.unlockAll.isVisible = false
        } else {
            binding.unlockAll.bind(
                theme = usingTheme,
                invert = true,
                text = getString(i18n.string.unlock_all),
                onAction = {
                    lifecycleScope.launch {
                        billingManager.charge(this@ThemeActivity)
                    }
                    gameAudioManager.playClickSound()
                },
            )

            lifecycleScope.launch {
                billingManager.getPriceFlow().collect {
                    binding.unlockAll.bind(
                        theme = usingTheme,
                        invert = true,
                        text = getString(i18n.string.unlock_all),
                        price = it.price,
                        showOffer = it.offer,
                        onAction = {
                            lifecycleScope.launch {
                                billingManager.charge(this@ThemeActivity)
                            }
                            gameAudioManager.playClickSound()
                        },
                    )
                }
            }
        }

        lifecycleScope.launch {
            val size = dimensionRepository.displayMetrics()
            val themesColumns =
                if (size.widthPixels > size.heightPixels) {
                    5
                } else {
                    3
                }
            val skinsColumns =
                if (size.widthPixels > size.heightPixels) {
                    2
                } else {
                    4
                }

            val themeAdapter =
                ThemeAdapter(
                    themeViewModel = themeViewModel,
                    preferencesRepository = preferencesRepository,
                    onSelectTheme = { theme ->
                        themeViewModel.sendEvent(ThemeEvent.ChangeTheme(theme))
                        gameAudioManager.playClickSound()
                    },
                    onRequestPurchase = {
                        lifecycleScope.launch {
                            billingManager.charge(this@ThemeActivity)
                        }
                        gameAudioManager.playMonetization()
                    },
                )

            val skinAdapter =
                SkinAdapter(
                    themeRepository = themeRepository,
                    themeViewModel = themeViewModel,
                    preferencesRepository = preferencesRepository,
                    onSelectSkin = { skin ->
                        themeViewModel.sendEvent(ThemeEvent.ChangeSkin(skin))
                        gameAudioManager.playClickSound()
                    },
                    onRequestPurchase = {
                        lifecycleScope.launch {
                            billingManager.charge(this@ThemeActivity)
                        }
                        gameAudioManager.playMonetization()
                    },
                )

            binding.themes.apply {
                addItemDecoration(SpaceItemDecoration(R.dimen.theme_divider))
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(context, themesColumns)
                adapter = themeAdapter
            }

            binding.skins.apply {
                addItemDecoration(SpaceItemDecoration(R.dimen.theme_divider))
                setHasFixedSize(true)
                layoutManager =
                    object : GridLayoutManager(context, skinsColumns) {
                        override fun checkLayoutParams(layoutParams: RecyclerView.LayoutParams?): Boolean {
                            val lpSize = binding.skins.measuredWidth / (skinsColumns + 1)
                            layoutParams?.height = lpSize
                            layoutParams?.width = lpSize
                            return true
                        }
                    }
                adapter = skinAdapter
            }

            if (!preferencesRepository.isPremiumEnabled()) {
                lifecycleScope.launch {
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
        val themes = binding.themes
        outState.putIntArray(SCROLL_VIEW_STATE, intArrayOf(themes.scrollX, themes.scrollY))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getIntArray(SCROLL_VIEW_STATE)?.let { position ->
            val themes = binding.themes
            themes.post { themes.scrollTo(position[0], position[1]) }
        }
    }

    companion object {
        const val SCROLL_VIEW_STATE = "SCROLL_VIEW_POSITION"
    }
}
