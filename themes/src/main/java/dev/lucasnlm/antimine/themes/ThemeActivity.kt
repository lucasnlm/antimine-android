package dev.lucasnlm.antimine.themes

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
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

class ThemeActivity : ThematicActivity(R.layout.activity_theme), SeekBar.OnSeekBarChangeListener {
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

        bindToolbar()

        if (preferencesRepository.isPremiumEnabled()) {
            unlockAll.visibility = View.GONE
        } else {
            unlockAll.bind(
                theme = usingTheme,
                invert = true,
                text = R.string.remove_ad,
                onAction = {
                    lifecycleScope.launch {
                        billingManager.charge(this@ThemeActivity)
                    }
                }
            )
        }

        squareSize.setOnSeekBarChangeListener(this)
        squareDivider.setOnSeekBarChangeListener(this)
        squareRadius.setOnSeekBarChangeListener(this)

        lifecycleScope.launchWhenCreated {
            val size = dimensionRepository.displaySize()
            val columns = if (size.width > size.height) { 5 } else { 3 }

            val themeAdapter = ThemeAdapter(
                activity = this@ThemeActivity,
                themeViewModel = themeViewModel,
                preferencesRepository = preferencesRepository,
                adsManager = adsManager
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

                    squareSize.progress = it.squareSize
                    squareDivider.progress = it.squareDivider
                    squareRadius.progress = it.squareRadius
                }
            }
        }
    }

    private fun bindToolbar() {
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
                bindToolbar()
            }
        )
    }

    override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            when (seekbar) {
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
        }
    }

    override fun onStartTrackingTouch(seekbar: SeekBar?) {
        // Empty
    }

    override fun onStopTrackingTouch(seekbar: SeekBar?) {
        // Empty
    }
}
