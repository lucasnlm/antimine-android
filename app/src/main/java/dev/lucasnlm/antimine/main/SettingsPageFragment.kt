package dev.lucasnlm.antimine.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.AboutActivity
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.history.HistoryActivity
import dev.lucasnlm.antimine.main.viewmodel.MainEvent
import dev.lucasnlm.antimine.main.viewmodel.MainViewModel
import dev.lucasnlm.antimine.preferences.PreferencesActivity
import dev.lucasnlm.antimine.stats.StatsActivity
import dev.lucasnlm.antimine.themes.ThemeActivity
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IFeatureFlagManager
import dev.lucasnlm.external.IPlayGamesManager
import kotlinx.android.synthetic.main.fragment_main_settings.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SettingsPageFragment : Fragment(R.layout.fragment_main_settings) {
    private val viewModel: MainViewModel by sharedViewModel()
    private val playGamesManager: IPlayGamesManager by inject()
    private val featureFlagManager: IFeatureFlagManager by inject()
    private val themeRepository: IThemeRepository by inject()
    private val analyticsManager: IAnalyticsManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usingTheme = themeRepository.getTheme()

        settings_menu.bind(
            text = R.string.more,
            startButton = R.drawable.back_arrow,
            startAction = {
                viewModel.sendEvent(MainEvent.GoToMainPageEvent)
            }
        )

        about_menu_text.bind(
            text = R.string.about,
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

        controls.bind(
            theme = usingTheme,
            text = R.string.control,
            startIcon = R.drawable.controls,
            onAction = {
                analyticsManager.sentEvent(Analytics.OpenControls)
                viewModel.sendEvent(MainEvent.ShowControlsEvent)
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

        about.bind(
            theme = usingTheme,
            text = R.string.about,
            startIcon = R.drawable.info,
            onAction = {
                analyticsManager.sentEvent(Analytics.OpenAbout)
                val intent = Intent(context, AboutActivity::class.java)
                startActivity(intent)
            }
        )

        translation.bind(
            theme = usingTheme,
            text = R.string.translation,
            startIcon = R.drawable.translate,
            onAction = {
                analyticsManager.sentEvent(Analytics.OpenTranslations)
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(CROWD_IN_URL))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT).show()
                }
            }
        )

        if (playGamesManager.hasGooglePlayGames()) {
            play_games.bind(
                theme = usingTheme,
                text = R.string.google_play_games,
                startIcon = R.drawable.games_controller,
                onAction = {
                    analyticsManager.sentEvent(Analytics.OpenGooglePlayGames)
                    viewModel.sendEvent(MainEvent.ShowGooglePlayGamesEvent)
                }
            )
        } else {
            play_games.visibility = View.GONE
        }
    }

    companion object {
        private const val CROWD_IN_URL = "https://crowdin.com/project/antimine-android"
    }
}
