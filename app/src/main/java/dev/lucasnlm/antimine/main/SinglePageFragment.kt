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
import dev.lucasnlm.antimine.history.HistoryActivity
import dev.lucasnlm.antimine.language.LanguageSelectorActivity
import dev.lucasnlm.antimine.main.viewmodel.MainEvent
import dev.lucasnlm.antimine.main.viewmodel.MainViewModel
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.PreferencesActivity
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.stats.StatsActivity
import dev.lucasnlm.antimine.themes.ThemeActivity
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.IFeatureFlagManager
import kotlinx.android.synthetic.main.fragment_main_new_game.*
import kotlinx.android.synthetic.main.fragment_main_new_game.settings
import kotlinx.android.synthetic.main.fragment_main_new_game.themes
import kotlinx.android.synthetic.main.fragment_main_settings.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SinglePageFragment : Fragment(R.layout.fragment_main_single_page) {
    private val viewModel: MainViewModel by sharedViewModel()
    private val analyticsManager: IAnalyticsManager by inject()
    private val themeRepository: IThemeRepository by inject()
    private val minefieldRepository: IMinefieldRepository by inject()
    private val dimensionRepository: IDimensionRepository by inject()
    private val preferencesRepository: IPreferencesRepository by inject()
    private val billingManager: IBillingManager by inject()
    private val savesRepository: ISavesRepository by inject()
    private val featureFlagManager: IFeatureFlagManager by inject()

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

    override fun onResume() {
        super.onResume()
        continue_game.requestFocus()
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

        settings.bind(
            theme = usingTheme,
            text = R.string.settings,
            startIcon = R.drawable.settings,
            onAction = {
                analyticsManager.sentEvent(Analytics.OpenSettings)
                val intent = Intent(context, PreferencesActivity::class.java)
                startActivity(intent)
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

        tutorial.bind(
            theme = usingTheme,
            text = R.string.tutorial,
            startIcon = R.drawable.tutorial,
            onAction = {
                analyticsManager.sentEvent(Analytics.OpenTutorial)
                viewModel.sendEvent(MainEvent.StartTutorialEvent)
            }
        )

        remove_ads.visibility = View.GONE
        if (featureFlagManager.isFoos) {
            remove_ads.apply {
                visibility = View.VISIBLE
                bind(
                    theme = usingTheme,
                    text = getString(R.string.donation),
                    startIcon = R.drawable.remove_ads,
                    onAction = {
                        lifecycleScope.launch {
                            activity?.let {
                                billingManager.charge(it)
                            }
                        }
                    }
                )
            }
        } else {
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

        if (featureFlagManager.isGameHistoryEnabled) {
            previous_games.bind(
                theme = usingTheme,
                text = R.string.previous_games,
                startIcon = R.drawable.old_games,
                onAction = {
                    analyticsManager.sentEvent(Analytics.OpenSaveHistory)
                    val intent = Intent(context, HistoryActivity::class.java)
                    startActivity(intent)
                }
            )
        } else {
            previous_games.visibility = View.GONE
        }

        stats.bind(
            theme = usingTheme,
            text = R.string.events,
            startIcon = R.drawable.stats,
            onAction = {
                analyticsManager.sentEvent(Analytics.OpenStats)
                val intent = Intent(context, StatsActivity::class.java)
                startActivity(intent)
            }
        )

        translation.bind(
            theme = usingTheme,
            text = R.string.translation,
            startIcon = R.drawable.translate,
            onAction = {
                analyticsManager.sentEvent(Analytics.OpenTranslations)
                startActivity(Intent(requireContext(), LanguageSelectorActivity::class.java))
            }
        )
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
                    lifecycleScope.launch {
                        activity?.let { billingManager.charge(it) }
                    }
                }
            )
        }
    }
}
