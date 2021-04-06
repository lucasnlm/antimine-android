package dev.lucasnlm.antimine.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.AboutActivity
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.control.ControlActivity
import dev.lucasnlm.antimine.custom.CustomLevelDialogFragment
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.history.HistoryActivity
import dev.lucasnlm.antimine.language.LanguageSelectorActivity
import dev.lucasnlm.antimine.main.viewmodel.MainEvent
import dev.lucasnlm.antimine.main.viewmodel.MainViewModel
import dev.lucasnlm.antimine.playgames.PlayGamesDialogFragment
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.PreferencesActivity
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.splash.SplashActivity
import dev.lucasnlm.antimine.stats.StatsActivity
import dev.lucasnlm.antimine.themes.ThemeActivity
import dev.lucasnlm.antimine.ui.ThematicActivity
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.IFeatureFlagManager
import dev.lucasnlm.external.IInAppUpdateManager
import dev.lucasnlm.external.IPlayGamesManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ThematicActivity(R.layout.activity_main) {
    private val viewModel: MainViewModel by viewModel()
    private val playGamesManager: IPlayGamesManager by inject()
    private val preferencesRepository: IPreferencesRepository by inject()
    private val minefieldRepository: IMinefieldRepository by inject()
    private val dimensionRepository: IDimensionRepository by inject()
    private val analyticsManager: IAnalyticsManager by inject()
    private val featureFlagManager: IFeatureFlagManager by inject()
    private val billingManager: IBillingManager by inject()
    private val savesRepository: ISavesRepository by inject()
    private val inAppUpdateManager: IInAppUpdateManager by inject()

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        continueGame.bind(
            theme = usingTheme,
            invert = true,
            text = R.string.start,
            onAction = {
                viewModel.sendEvent(MainEvent.ContinueGameEvent)
            }
        )

        lifecycleScope.launch {
            savesRepository.fetchCurrentSave()?.let {
                continueGame.bind(
                    theme = usingTheme,
                    invert = true,
                    text = R.string.continue_game,
                    onAction = {
                        viewModel.sendEvent(MainEvent.ContinueGameEvent)
                    }
                )
            }
        }

        newGameShow.bind(
            theme = usingTheme,
            text = getString(R.string.new_game),
            startIcon = R.drawable.more,
            onAction = {
                newGameShow.visibility = View.GONE
                difficulties.visibility = View.VISIBLE
            }
        )

        difficulties.strokeColor = usingTheme.palette.covered.toAndroidColor()

        startStandard.setRadius(5f)
        startStandard.bind(
            theme = usingTheme,
            text = getString(R.string.standard),
            extra = getDifficultyExtra(Difficulty.Standard),
            onAction = {
                viewModel.sendEvent(
                    MainEvent.StartNewGameEvent(difficulty = Difficulty.Standard)
                )
            }
        )

        startBeginner.setRadius(5f)
        startBeginner.bind(
            theme = usingTheme,
            text = getString(R.string.beginner),
            extra = getDifficultyExtra(Difficulty.Beginner),
            onAction = {
                viewModel.sendEvent(
                    MainEvent.StartNewGameEvent(difficulty = Difficulty.Beginner)
                )
            }
        )

        startIntermediate.setRadius(5f)
        startIntermediate.bind(
            theme = usingTheme,
            text = getString(R.string.intermediate),
            extra = getDifficultyExtra(Difficulty.Intermediate),
            onAction = {
                viewModel.sendEvent(
                    MainEvent.StartNewGameEvent(difficulty = Difficulty.Intermediate)
                )
            }
        )

        startExpert.setRadius(5f)
        startExpert.bind(
            theme = usingTheme,
            text = getString(R.string.expert),
            extra = getDifficultyExtra(Difficulty.Expert),
            onAction = {
                viewModel.sendEvent(
                    MainEvent.StartNewGameEvent(difficulty = Difficulty.Expert)
                )
            }
        )

        startCustom.setRadius(5f)
        startCustom.bind(
            theme = usingTheme,
            text = getString(R.string.custom),
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
                val intent = Intent(this, PreferencesActivity::class.java)
                startActivity(intent)
            }
        )

        themes.bind(
            theme = usingTheme,
            text = R.string.themes,
            startIcon = R.drawable.themes,
            onAction = {
                analyticsManager.sentEvent(Analytics.OpenThemes)
                val intent = Intent(this, ThemeActivity::class.java)
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

        removeAds.visibility = View.GONE
        if (featureFlagManager.isFoos) {
            removeAds.apply {
                visibility = View.VISIBLE
                bind(
                    theme = usingTheme,
                    text = getString(R.string.donation),
                    startIcon = R.drawable.remove_ads,
                    onAction = {
                        lifecycleScope.launch {
                            billingManager.charge(this@MainActivity)
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
            previousGames.bind(
                theme = usingTheme,
                text = R.string.previous_games,
                startIcon = R.drawable.old_games,
                onAction = {
                    analyticsManager.sentEvent(Analytics.OpenSaveHistory)
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                }
            )
        } else {
            previousGames.visibility = View.GONE
        }

        tutorial.bind(
            theme = usingTheme,
            text = R.string.tutorial,
            startIcon = R.drawable.tutorial,
            onAction = {
                analyticsManager.sentEvent(Analytics.OpenTutorial)
                viewModel.sendEvent(MainEvent.StartTutorialEvent)
            }
        )

        stats.bind(
            theme = usingTheme,
            text = R.string.events,
            startIcon = R.drawable.stats,
            onAction = {
                analyticsManager.sentEvent(Analytics.OpenStats)
                val intent = Intent(this, StatsActivity::class.java)
                startActivity(intent)
            }
        )

        about.bind(
            theme = usingTheme,
            text = R.string.about,
            startIcon = R.drawable.info,
            onAction = {
                analyticsManager.sentEvent(Analytics.OpenAbout)
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
            }
        )

        translation.bind(
            theme = usingTheme,
            text = R.string.translation,
            startIcon = R.drawable.translate,
            onAction = {
                analyticsManager.sentEvent(Analytics.OpenTranslations)
                startActivity(Intent(this, LanguageSelectorActivity::class.java))
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

        lifecycleScope.launchWhenCreated {
            viewModel
                .observeSideEffects()
                .collect(::handleSideEffects)
        }

        launchGooglePlayGames()
    }

    private fun getDifficultyExtra(difficulty: Difficulty): String {
        return minefieldRepository.fromDifficulty(
            difficulty,
            dimensionRepository,
            preferencesRepository,
        ).toExtraString()
    }

    private fun Minefield.toExtraString(): String {
        return "${this.width} × ${this.height} - ${this.mines}"
    }

    private fun handleSideEffects(event: MainEvent) {
        when (event) {
            is MainEvent.ShowCustomDifficultyDialogEvent -> {
                showCustomLevelDialog()
            }
            is MainEvent.GoToMainPageEvent -> {
                viewPager.setCurrentItem(0, true)
            }
            is MainEvent.GoToSettingsPageEvent -> {
                viewPager.setCurrentItem(1, true)
            }
            is MainEvent.ShowControlsEvent -> {
                showControlDialog()
            }
            is MainEvent.ShowGooglePlayGamesEvent -> {
                showGooglePlayGames()
            }
            is MainEvent.Recreate -> {
                finish()
                startActivity(Intent(this, SplashActivity::class.java))
                overridePendingTransition(0, 0)
            }
            else -> {
            }
        }
    }

    private fun showCustomLevelDialog() {
        if (supportFragmentManager.findFragmentByTag(CustomLevelDialogFragment.TAG) == null && !isFinishing) {
            CustomLevelDialogFragment().apply {
                show(supportFragmentManager, CustomLevelDialogFragment.TAG)
            }
        }
    }

    private fun showControlDialog() {
        val intent = Intent(this, ControlActivity::class.java)
        startActivity(intent)
    }

    private fun showGooglePlayGames() {
        if (playGamesManager.isLogged()) {
            if (supportFragmentManager.findFragmentByTag(PlayGamesDialogFragment.TAG) == null && !isFinishing) {
                PlayGamesDialogFragment().show(supportFragmentManager, PlayGamesDialogFragment.TAG)
            }
        } else {
            playGamesManager.getLoginIntent()?.let {
                ActivityCompat.startActivityForResult(this, it, RC_GOOGLE_PLAY, null)
            }
        }
    }

    private fun afterGooglePlayGames() {
        inAppUpdateManager.checkUpdate(this)
    }

    private fun launchGooglePlayGames() {
        if (playGamesManager.hasGooglePlayGames() && playGamesManager.shouldRequestLogin()) {
            playGamesManager.keepRequestingLogin(false)

            lifecycleScope.launchWhenCreated {
                var logged: Boolean

                try {
                    withContext(Dispatchers.IO) {
                        logged = playGamesManager.silentLogin()
                        if (logged) {
                            refreshUserId()
                        }
                        playGamesManager.showPlayPopUp(this@MainActivity)
                    }
                } catch (e: Exception) {
                    logged = false
                    Log.e(SplashActivity.TAG, "Failed silent login", e)
                }

                if (!logged) {
                    try {
                        playGamesManager.getLoginIntent()?.let {
                            ActivityCompat.startActivityForResult(
                                this@MainActivity, it,
                                RC_GOOGLE_PLAY, null
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(SplashActivity.TAG, "User not logged or doesn't have Play Games", e)
                    }
                } else {
                    afterGooglePlayGames()
                }
            }
        } else {
            afterGooglePlayGames()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_PLAY) {
            playGamesManager.handleLoginResult(data)
            lifecycleScope.launch {
                refreshUserId()
            }
        }
    }

    private fun bindRemoveAds(price: String? = null) {
        removeAds.apply {
            visibility = View.VISIBLE
            bind(
                theme = usingTheme,
                text = getString(R.string.remove_ad),
                startIcon = R.drawable.remove_ads,
                extra = price,
                onAction = {
                    lifecycleScope.launch {
                        billingManager.charge(this@MainActivity)
                    }
                }
            )
        }
    }

    private suspend fun refreshUserId() {
        withContext(Dispatchers.Default) {
            val lastId = preferencesRepository.userId()
            val newId = playGamesManager.playerId()

            if (lastId != newId && newId != null) {
                preferencesRepository.setUserId(newId)

                withContext(Dispatchers.Main) {
                    migrateDataAndRecreate()
                }
            }
        }
    }

    private fun migrateDataAndRecreate() {
        lifecycleScope.launchWhenCreated {
            if (!isFinishing) {
                preferencesRepository.userId()?.let {
                    viewModel.sendEvent(MainEvent.FetchCloudSave(it))
                }
            }
        }
    }

    override fun onBackPressed() {
        if (newGameShow.visibility == View.GONE) {
            newGameShow.visibility = View.VISIBLE
            difficulties.visibility = View.GONE
        } else {
            finishAffinity()
        }
    }

    companion object {
        private const val RC_GOOGLE_PLAY = 6
    }
}
