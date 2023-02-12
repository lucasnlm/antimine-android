package dev.lucasnlm.antimine.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import dev.lucasnlm.antimine.GameActivity
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.AboutActivity
import dev.lucasnlm.antimine.common.level.database.models.SaveStatus
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.control.ControlActivity
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.custom.CustomLevelDialogFragment
import dev.lucasnlm.antimine.history.HistoryActivity
import dev.lucasnlm.antimine.main.viewmodel.MainEvent
import dev.lucasnlm.antimine.main.viewmodel.MainViewModel
import dev.lucasnlm.antimine.playgames.PlayGamesDialogFragment
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.PreferencesActivity
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.stats.StatsActivity
import dev.lucasnlm.antimine.themes.ThemeActivity
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.external.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ThemedActivity(R.layout.activity_main) {
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
    private val instantAppManager: IInstantAppManager by inject()
    private val preferenceRepository: IPreferencesRepository by inject()

    private lateinit var viewPager: ViewPager2

    private lateinit var googlePlayLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googlePlayLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                handlePlayGames(result.data)
            }
        }

        continueGame.apply {
            if (preferencesRepository.showContinueGame()) {
                setText(R.string.continue_game)
            } else {
                setText(R.string.start)
            }

            setOnClickListener {
                viewModel.sendEvent(MainEvent.ContinueGameEvent)
            }
        }

        if (!preferencesRepository.showContinueGame()) {
            lifecycleScope.launch {
                savesRepository.fetchCurrentSave()?.let {
                    preferencesRepository.setContinueGameLabel(true)
                    continueGame.setText(R.string.continue_game)
                }
            }
        }

        lifecycleScope.launch {
            if (preferencesRepository.showTutorialButton()) {
                val shouldShowTutorial = savesRepository.getAllSaves().count { it.status == SaveStatus.VICTORY } < 2
                preferencesRepository.setShowTutorialButton(shouldShowTutorial)
                withContext(Dispatchers.Main) {
                    if (!shouldShowTutorial) {
                        tutorial.visibility = View.GONE
                    }
                }
            } else {
                tutorial.visibility = View.GONE
            }
        }

        newGameShow.setOnClickListener {
            newGameShow.visibility = View.GONE
            difficulties.visibility = View.VISIBLE
        }

        mapOf(
            standardSize to Difficulty.Standard,
            fixedSizeSize to Difficulty.FixedSize,
            beginnerSize to Difficulty.Beginner,
            intermediateSize to Difficulty.Intermediate,
            expertSize to Difficulty.Expert,
            masterSize to Difficulty.Master,
            legendSize to Difficulty.Legend,
        ).onEach {
            it.key.text = getDifficultyExtra(it.value)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && instantAppManager.isEnabled(applicationContext)) {
            listOf(
                Difficulty.Beginner,
                Difficulty.Intermediate,
                Difficulty.Expert,
                Difficulty.Master,
            ).forEach(::pushShortcutOf)
        }

        mapOf(
            startStandard to Difficulty.Standard,
            startFixedSize to Difficulty.FixedSize,
            startBeginner to Difficulty.Beginner,
            startIntermediate to Difficulty.Intermediate,
            startExpert to Difficulty.Expert,
            startMaster to Difficulty.Master,
            startLegend to Difficulty.Legend,
        ).forEach { (view, difficulty) ->
            view.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    pushShortcutOf(difficulty)
                }

                viewModel.sendEvent(
                    MainEvent.StartNewGameEvent(difficulty = difficulty),
                )
            }
        }

        startCustom.setOnClickListener {
            analyticsManager.sentEvent(Analytics.OpenCustom)
            viewModel.sendEvent(MainEvent.ShowCustomDifficultyDialogEvent)
        }

        settings.setOnClickListener {
            analyticsManager.sentEvent(Analytics.OpenSettings)
            val intent = Intent(this, PreferencesActivity::class.java)
            startActivity(intent)
        }

        themes.setOnClickListener {
            val intent = Intent(this, ThemeActivity::class.java)
            preferencesRepository.setNewThemesIcon(false)
            startActivity(intent)
        }

        newThemesIcon.visibility = if (preferencesRepository.showNewThemesIcon()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        controls.setOnClickListener {
            analyticsManager.sentEvent(Analytics.OpenControls)
            viewModel.sendEvent(MainEvent.ShowControlsEvent)
        }

        if (featureFlagManager.isFoss) {
            removeAdsRoot.visibility = View.VISIBLE
            removeAds.apply {
                setOnClickListener {
                    lifecycleScope.launch {
                        billingManager.charge(this@MainActivity)
                    }
                }
                text = getString(R.string.donation)
                setIconResource(R.drawable.remove_ads)
            }
        } else {
            if (!preferencesRepository.isPremiumEnabled() && billingManager.isEnabled()) {
                billingManager.start()

                lifecycleScope.launchWhenResumed {
                    bindRemoveAds()

                    billingManager.getPriceFlow().collect {
                        bindRemoveAds(it.price, it.offer)
                    }
                }
            }
        }

        if (featureFlagManager.isGameHistoryEnabled) {
            previousGames.setOnClickListener {
                analyticsManager.sentEvent(Analytics.OpenSaveHistory)
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
            }
        } else {
            previousGames.visibility = View.GONE
        }

        tutorial.apply {
            setText(R.string.tutorial)
            setOnClickListener {
                analyticsManager.sentEvent(Analytics.OpenTutorial)
                viewModel.sendEvent(MainEvent.StartTutorialEvent)
            }
        }

        stats.setOnClickListener {
            analyticsManager.sentEvent(Analytics.OpenStats)
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
        }

        about.setOnClickListener {
            analyticsManager.sentEvent(Analytics.OpenAbout)
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        if (playGamesManager.hasGooglePlayGames()) {
            play_games.setOnClickListener {
                analyticsManager.sentEvent(Analytics.OpenGooglePlayGames)
                viewModel.sendEvent(MainEvent.ShowGooglePlayGamesEvent)
            }
        } else {
            play_games.visibility = View.GONE
        }

        lifecycleScope.launchWhenCreated {
            viewModel
                .observeSideEffects()
                .collect(::handleSideEffects)
        }

        launchGooglePlayGames()

        onBackPressedDispatcher.addCallback {
            handleBackPressed()
        }

        redirectToGame()
    }

    private fun pushShortcutOf(difficulty: Difficulty) {
        if (instantAppManager.isEnabled(applicationContext)) {
            // Ignore. Instant App doesn't support shortcuts.
            return
        }

        val idLow = difficulty.id.lowercase()
        val deeplink = Uri.parse("app://antimine/game?difficulty=$idLow")

        val name = when (difficulty) {
            Difficulty.Beginner -> R.string.beginner
            Difficulty.Intermediate -> R.string.intermediate
            Difficulty.Expert -> R.string.expert
            Difficulty.Master -> R.string.master
            Difficulty.Legend -> R.string.legend
            else -> return
        }

        val icon = when (difficulty) {
            Difficulty.Beginner -> R.mipmap.shortcut_one
            Difficulty.Intermediate -> R.mipmap.shortcut_two
            Difficulty.Expert -> R.mipmap.shortcut_three
            Difficulty.Master -> R.mipmap.shortcut_four
            Difficulty.Legend -> R.mipmap.shortcut_four
            else -> return
        }

        val shortcut = ShortcutInfoCompat.Builder(applicationContext, difficulty.id)
            .setShortLabel(getString(name))
            .setIcon(IconCompat.createWithResource(applicationContext, icon))
            .setIntent(Intent(Intent.ACTION_VIEW, deeplink))
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, shortcut)
    }

    override fun onResume() {
        super.onResume()

        if (newGameShow.visibility == View.GONE) {
            newGameShow.visibility = View.VISIBLE
            difficulties.visibility = View.GONE
        }
    }

    private fun getDifficultyExtra(difficulty: Difficulty): String {
        return minefieldRepository.fromDifficulty(
            difficulty,
            dimensionRepository,
            preferencesRepository,
        ).toExtraString()
    }

    private fun redirectToGame() {
        val playGames = playGamesManager.hasGooglePlayGames()
        if ((playGames && preferencesRepository.userId() != null || !playGames) &&
            preferencesRepository.openGameDirectly()
        ) {
            Intent(this, GameActivity::class.java).run { startActivity(this) }
        }
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
                startActivity(Intent(this, MainActivity::class.java))
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
                googlePlayLauncher.launch(it)
            }
        }
    }

    private fun afterGooglePlayGames() {
        playGamesManager.signInToFirebase(this)
        inAppUpdateManager.checkUpdate(this)
    }

    private fun launchGooglePlayGames() {
        if (playGamesManager.hasGooglePlayGames() &&
            playGamesManager.shouldRequestLogin() &&
            preferenceRepository.keepRequestPlayGames()
        ) {
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
                    Log.e(TAG, "Failed silent login", e)
                }

                if (!logged) {
                    try {
                        playGamesManager.getLoginIntent()?.let {
                            googlePlayLauncher.launch(it)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "User not logged or doesn't have Play Games", e)
                    }
                } else {
                    afterGooglePlayGames()
                }
            }
        } else {
            afterGooglePlayGames()
        }
    }

    private fun handlePlayGames(data: Intent?) {
        playGamesManager.handleLoginResult(data)
        lifecycleScope.launch {
            refreshUserId()
        }
    }

    private fun bindRemoveAds(price: String? = null, showOffer: Boolean = false) {
        removeAdsRoot.visibility = View.VISIBLE
        removeAds.apply {
            setOnClickListener {
                lifecycleScope.launch {
                    billingManager.charge(this@MainActivity)
                }
            }
            setText(R.string.remove_ad)
            setIconResource(R.drawable.remove_ads)

            price?.let {
                priceText.text = it
                priceText.visibility = View.VISIBLE
            }

            if (showOffer) {
                priceOff.visibility = View.VISIBLE
            }
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

    private fun handleBackPressed() {
        if (newGameShow.visibility == View.GONE) {
            newGameShow.visibility = View.VISIBLE
            difficulties.visibility = View.GONE
        } else {
            finishAffinity()
        }
    }

    companion object {
        val TAG = MainActivity::class.simpleName
    }
}
