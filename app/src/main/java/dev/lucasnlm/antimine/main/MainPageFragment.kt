package dev.lucasnlm.antimine.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.main.viewmodel.MainEvent
import dev.lucasnlm.antimine.main.viewmodel.MainViewModel
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.purchases.SupportAppDialogFragment
import dev.lucasnlm.antimine.themes.ThemeActivity
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IBillingManager
import kotlinx.android.synthetic.main.fragment_main_new_game.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MainPageFragment : Fragment(R.layout.fragment_main_new_game) {
    private val viewModel: MainViewModel by sharedViewModel()
    private val analyticsManager: IAnalyticsManager by inject()
    private val themeRepository: IThemeRepository by inject()
    private val minefieldRepository: IMinefieldRepository by inject()
    private val dimensionRepository: IDimensionRepository by inject()
    private val preferencesRepository: IPreferencesRepository by inject()
    private val billingManager: IBillingManager by inject()
    private val savesRepository: ISavesRepository by inject()

    private fun getDifficultyExtra(difficulty: Difficulty): String {
        return minefieldRepository.fromDifficulty(
            difficulty,
            dimensionRepository,
            preferencesRepository,
        ).toExtraString()
    }

    private fun Minefield.toExtraString(): String {
        return "${this.width}x${this.height} - ${this.mines}"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usingTheme = themeRepository.getTheme()

        main.bind(
            text = R.string.app_name,
        )

        new_game.bind(
            text = R.string.new_game,
        )

        general.bind(
            text = R.string.general,
        )

        continue_game.bind(
            theme = usingTheme,
            invert = true,
            text = R.string.start,
            onAction = {
                viewModel.sendEvent(MainEvent.ContinueGameEvent)
            }
        )

        lifecycleScope.launch {
            savesRepository.fetchCurrentSave()?.let {
                continue_game.bind(
                    theme = usingTheme,
                    invert = true,
                    text = R.string.continue_game,
                    onAction = {
                        viewModel.sendEvent(MainEvent.ContinueGameEvent)
                    }
                )
            }
        }

        standard.bind(
            theme = usingTheme,
            text = getString(R.string.standard),
            extra = getDifficultyExtra(Difficulty.Standard),
            onAction = {
                viewModel.sendEvent(
                    MainEvent.StartNewGameEvent(difficulty = Difficulty.Standard)
                )
            }
        )

        beginner.bind(
            theme = usingTheme,
            text = getString(R.string.beginner),
            extra = getDifficultyExtra(Difficulty.Beginner),
            onAction = {
                viewModel.sendEvent(
                    MainEvent.StartNewGameEvent(difficulty = Difficulty.Beginner)
                )
            }
        )

        intermediate.bind(
            theme = usingTheme,
            text = getString(R.string.intermediate),
            extra = getDifficultyExtra(Difficulty.Intermediate),
            onAction = {
                viewModel.sendEvent(
                    MainEvent.StartNewGameEvent(difficulty = Difficulty.Intermediate)
                )
            }
        )

        expert.bind(
            theme = usingTheme,
            text = getString(R.string.expert),
            extra = getDifficultyExtra(Difficulty.Expert),
            onAction = {
                viewModel.sendEvent(
                    MainEvent.StartNewGameEvent(difficulty = Difficulty.Expert)
                )
            }
        )

        custom.bind(
            theme = usingTheme,
            text = R.string.custom,
            onAction = {
                analyticsManager.sentEvent(Analytics.OpenCustom)
                viewModel.sendEvent(MainEvent.ShowCustomDifficultyDialogEvent)
            }
        )

        tutorial.bind(
            theme = usingTheme,
            text = R.string.tutorial,
            startIcon = R.drawable.tutorial,
            onAction = {
                analyticsManager.sentEvent(Analytics.OpenTutorial)
                viewModel.sendEvent(MainEvent.StartTutorialEvent)
            }
        )

        settings.bind(
            theme = usingTheme,
            text = R.string.settings,
            endIcon = R.drawable.arrow_right,
            onAction = {
                viewModel.sendEvent(MainEvent.GoToSettingsPageEvent)
            }
        )

        themes.bind(
            theme = usingTheme,
            text = R.string.themes,
            startIcon = R.drawable.themes,
            onAction = {
                analyticsManager.sentEvent(Analytics.OpenThemes)
                val intent = Intent(context, ThemeActivity::class.java)
                startActivity(intent)
            }
        )

        remove_ads.visibility = View.GONE
        if (!preferencesRepository.isPremiumEnabled() && billingManager.isEnabled()) {
            billingManager.start()

            lifecycleScope.launchWhenResumed {
                bindRemoveAds()

                billingManager.getPriceFlow().collect {
                    bindRemoveAds(it)
                }
            }
        }
    }

    private fun bindRemoveAds(price: String? = null) {
        val usingTheme = themeRepository.getTheme()

        remove_ads.apply {
            visibility = View.VISIBLE
            bind(
                theme = usingTheme,
                text = getString(R.string.remove_ad),
                startIcon = R.drawable.remove_ads,
                extra = price,
                onAction = {
                    SupportAppDialogFragment.newRemoveAdsSupportDialog(
                        context,
                        price,
                    ).show(parentFragmentManager, SupportAppDialogFragment.TAG)
                }
            )
        }
    }
}
