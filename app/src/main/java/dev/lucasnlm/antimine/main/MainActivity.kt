package dev.lucasnlm.antimine.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import dev.lucasnlm.antimine.GameActivity
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.AboutActivity
import dev.lucasnlm.antimine.common.level.database.models.SaveStatus
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.SavesRepository
import dev.lucasnlm.antimine.control.ControlActivity
import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.custom.CustomLevelDialogFragment
import dev.lucasnlm.antimine.databinding.ActivityMainBinding
import dev.lucasnlm.antimine.history.HistoryActivity
import dev.lucasnlm.antimine.l10n.GameLocaleManager
import dev.lucasnlm.antimine.main.viewmodel.MainEvent
import dev.lucasnlm.antimine.main.viewmodel.MainViewModel
import dev.lucasnlm.antimine.playgames.PlayGamesDialogFragment
import dev.lucasnlm.antimine.preferences.PreferencesActivity
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.stats.StatsActivity
import dev.lucasnlm.antimine.themes.ThemeActivity
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.external.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import dev.lucasnlm.antimine.common.R as CR
import dev.lucasnlm.antimine.i18n.R as i18n

class MainActivity : ThemedActivity() {
    private val viewModel: MainViewModel by viewModel()
    private val playGamesManager: PlayGamesManager by inject()
    private val preferencesRepository: PreferencesRepository by inject()
    private val minefieldRepository: MinefieldRepository by inject()
    private val dimensionRepository: DimensionRepository by inject()
    private val analyticsManager: AnalyticsManager by inject()
    private val featureFlagManager: FeatureFlagManager by inject()
    private val billingManager: BillingManager by inject()
    private val savesRepository: SavesRepository by inject()
    private val inAppUpdateManager: InAppUpdateManager by inject()
    private val instantAppManager: InstantAppManager by inject()
    private val preferenceRepository: PreferencesRepository by inject()
    private val soundManager: GameAudioManager by inject()
    private val gameLocaleManager: GameLocaleManager by inject()

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var viewPager: ViewPager2
    private lateinit var googlePlayLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Must be called after onCreate
        gameLocaleManager.applyPreferredLocaleIfNeeded()

        setContentView(binding.root)

        googlePlayLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    handlePlayGames(result.data)
                }
            }

        bindMenuButtons()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && instantAppManager.isEnabled(applicationContext)) {
            listOf(
                Difficulty.Beginner,
                Difficulty.Intermediate,
                Difficulty.Expert,
                Difficulty.Master,
            ).forEach(::pushShortcutOf)
        }

        lifecycleScope.launch {
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

    private fun bindMenuButtons() {
        binding.continueGame.apply {
            if (preferencesRepository.showContinueGame()) {
                setText(i18n.string.continue_game)
            } else {
                setText(i18n.string.start)
            }

            setOnClickListener {
                soundManager.playClickSound()
                viewModel.sendEvent(MainEvent.ContinueGameEvent)
            }
        }

        if (!preferencesRepository.showContinueGame()) {
            lifecycleScope.launch {
                savesRepository.fetchCurrentSave()?.let {
                    preferencesRepository.setContinueGameLabel(true)
                    binding.continueGame.setText(i18n.string.continue_game)
                }
            }
        }

        lifecycleScope.launch {
            if (preferencesRepository.showTutorialButton()) {
                val shouldShowTutorial = savesRepository.getAllSaves().count { it.status == SaveStatus.VICTORY } < 2
                preferencesRepository.setShowTutorialButton(shouldShowTutorial)
                withContext(Dispatchers.Main) {
                    binding.tutorial.isVisible = shouldShowTutorial
                }
            } else {
                binding.tutorial.isVisible = false
            }
        }

        binding.newGameShow.setOnClickListener {
            soundManager.playClickSound()
            binding.newGameShow.isVisible = false
            binding.difficulties.isVisible = true
        }

        mapOf(
            binding.standardSize to Difficulty.Standard,
            binding.fixedSizeSize to Difficulty.FixedSize,
            binding.beginnerSize to Difficulty.Beginner,
            binding.intermediateSize to Difficulty.Intermediate,
            binding.expertSize to Difficulty.Expert,
            binding.masterSize to Difficulty.Master,
            binding.legendSize to Difficulty.Legend,
        ).onEach {
            it.key.text = getDifficultyExtra(it.value)
        }

        mapOf(
            binding.startStandard to Difficulty.Standard,
            binding.startFixedSize to Difficulty.FixedSize,
            binding.startBeginner to Difficulty.Beginner,
            binding.startIntermediate to Difficulty.Intermediate,
            binding.startExpert to Difficulty.Expert,
            binding.startMaster to Difficulty.Master,
            binding.startLegend to Difficulty.Legend,
        ).forEach { (view, difficulty) ->
            view.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    pushShortcutOf(difficulty)
                }

                soundManager.playClickSound()

                viewModel.sendEvent(
                    MainEvent.StartNewGameEvent(difficulty = difficulty),
                )
            }
        }

        binding.startCustom.setOnClickListener {
            soundManager.playClickSound()
            analyticsManager.sentEvent(Analytics.OpenCustom)
            viewModel.sendEvent(MainEvent.ShowCustomDifficultyDialogEvent)
        }

        binding.settings.setOnClickListener {
            soundManager.playClickSound()
            analyticsManager.sentEvent(Analytics.OpenSettings)
            val intent = Intent(this, PreferencesActivity::class.java)
            startActivity(intent)
        }

        binding.themes.setOnClickListener {
            soundManager.playClickSound()
            val intent = Intent(this, ThemeActivity::class.java)
            preferencesRepository.setNewThemesIcon(false)
            startActivity(intent)
        }

        binding.newThemesIcon.isVisible = preferencesRepository.showNewThemesIcon()

        binding.controls.setOnClickListener {
            soundManager.playClickSound()
            analyticsManager.sentEvent(Analytics.OpenControls)
            viewModel.sendEvent(MainEvent.ShowControlsEvent)
        }

        if (featureFlagManager.isFoss) {
            binding.removeAdsRoot.isVisible = true
            binding.removeAds.apply {
                setOnClickListener {
                    soundManager.playClickSound()
                    lifecycleScope.launch {
                        billingManager.charge(this@MainActivity)
                    }
                }
                text = getString(i18n.string.donation)
                setIconResource(R.drawable.remove_ads)
            }
        } else {
            if (!preferencesRepository.isPremiumEnabled() && billingManager.isEnabled()) {
                billingManager.start()

                lifecycleScope.launch {
                    bindRemoveAds()

                    billingManager.getPriceFlow().collect {
                        bindRemoveAds(it.price, it.offer)
                    }
                }
            }
        }

        if (featureFlagManager.isGameHistoryEnabled) {
            binding.previousGames.setOnClickListener {
                soundManager.playClickSound()
                analyticsManager.sentEvent(Analytics.OpenSaveHistory)
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
            }
        } else {
            binding.previousGames.isVisible = false
        }

        binding.tutorial.apply {
            setText(i18n.string.tutorial)
            setOnClickListener {
                soundManager.playClickSound()
                analyticsManager.sentEvent(Analytics.OpenTutorial)
                viewModel.sendEvent(MainEvent.StartTutorialEvent)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.language.apply {
                setText(i18n.string.language)
                setOnClickListener {
                    soundManager.playClickSound()
                    analyticsManager.sentEvent(Analytics.OpenLanguage)
                    viewModel.sendEvent(MainEvent.StartLanguageEvent)
                }
            }
        } else {
            binding.language.isVisible = false
        }

        binding.stats.setOnClickListener {
            soundManager.playClickSound()
            analyticsManager.sentEvent(Analytics.OpenStats)
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
        }

        binding.about.setOnClickListener {
            soundManager.playClickSound()
            analyticsManager.sentEvent(Analytics.OpenAbout)
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        if (playGamesManager.hasGooglePlayGames()) {
            binding.playGames.setOnClickListener {
                soundManager.playClickSound()
                analyticsManager.sentEvent(Analytics.OpenGooglePlayGames)
                viewModel.sendEvent(MainEvent.ShowGooglePlayGamesEvent)
            }
        } else {
            binding.playGames.isVisible = false
        }
    }

    /**
     * Pushes a shortcut to the Home launcher.
     * @param difficulty The difficulty to be used as a shortcut.
     */
    private fun pushShortcutOf(difficulty: Difficulty) {
        if (instantAppManager.isEnabled(applicationContext)) {
            // Ignore. Instant App doesn't support shortcuts.
            return
        }

        val idLow = difficulty.id.lowercase()
        val deeplink = Uri.parse("app://antimine/game?difficulty=$idLow")

        val name =
            when (difficulty) {
                Difficulty.Beginner -> i18n.string.beginner
                Difficulty.Intermediate -> i18n.string.intermediate
                Difficulty.Expert -> i18n.string.expert
                Difficulty.Master -> i18n.string.master
                Difficulty.Legend -> i18n.string.legend
                else -> return
            }

        val icon =
            when (difficulty) {
                Difficulty.Beginner -> CR.mipmap.shortcut_one
                Difficulty.Intermediate -> CR.mipmap.shortcut_two
                Difficulty.Expert -> CR.mipmap.shortcut_three
                Difficulty.Master -> CR.mipmap.shortcut_four
                Difficulty.Legend -> CR.mipmap.shortcut_four
                else -> return
            }

        val shortcut =
            ShortcutInfoCompat.Builder(applicationContext, difficulty.id)
                .setShortLabel(getString(name))
                .setIcon(IconCompat.createWithResource(applicationContext, icon))
                .setIntent(Intent(Intent.ACTION_VIEW, deeplink))
                .build()

        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, shortcut)
    }

    override fun onResume() {
        super.onResume()

        if (!binding.newGameShow.isVisible) {
            binding.newGameShow.isVisible = true
            binding.difficulties.isVisible = false
        }
    }

    private fun getDifficultyExtra(difficulty: Difficulty): String {
        return minefieldRepository.fromDifficulty(
            difficulty,
            dimensionRepository,
            preferencesRepository,
        ).toExtraString()
    }

    private fun canOpenGameDirectly(): Boolean {
        val playGames = playGamesManager.hasGooglePlayGames()
        val openDirectly = preferencesRepository.openGameDirectly()
        return (playGames && preferencesRepository.userId() != null || !playGames) && openDirectly
    }

    private fun redirectToGame() {
        if (canOpenGameDirectly()) {
            Intent(this, GameActivity::class.java).run { startActivity(this) }
        }
    }

    private fun Minefield.toExtraString(): String {
        return getString(i18n.string.minefield_with_mines_size, width, height, mines)
    }

    private fun handleSideEffects(event: MainEvent) {
        when (event) {
            is MainEvent.ShowCustomDifficultyDialogEvent -> {
                showCustomLevelDialog()
            }
            is MainEvent.GoToMainPageEvent -> {
                viewPager.setCurrentItem(0, true)
            }
            is MainEvent.OpenActivity -> {
                startActivity(event.intent)
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
                compatOverridePendingTransition()
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

            lifecycleScope.launch {
                var logged = false

                runCatching {
                    withContext(Dispatchers.IO) {
                        logged = playGamesManager.silentLogin()
                        if (logged) {
                            refreshUserId()
                        }
                        playGamesManager.showPlayPopUp(this@MainActivity)
                    }
                }.onFailure {
                    Log.e(TAG, "Failed silent login", it)
                }

                if (!logged) {
                    runCatching {
                        playGamesManager.getLoginIntent()?.let {
                            googlePlayLauncher.launch(it)
                        }
                    }.onFailure {
                        Log.e(TAG, "User not logged or doesn't have Play Games", it)
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

    private fun bindRemoveAds(
        price: String? = null,
        showOffer: Boolean = false,
    ) {
        binding.removeAdsRoot.isVisible = true
        binding.removeAds.apply {
            setOnClickListener {
                soundManager.playClickSound()
                lifecycleScope.launch {
                    billingManager.charge(this@MainActivity)
                }
            }
            setText(i18n.string.remove_ad)
            setIconResource(R.drawable.remove_ads)

            price?.let {
                binding.priceText.text = it
                binding.priceText.isVisible = true
            }

            binding.priceOff.isVisible = showOffer
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
        lifecycleScope.launch {
            if (!isFinishing) {
                preferencesRepository.userId()?.let {
                    viewModel.sendEvent(MainEvent.FetchCloudSave(it))
                }
            }
        }
    }

    private fun handleBackPressed() {
        if (!binding.newGameShow.isVisible) {
            binding.newGameShow.isVisible = true
            binding.difficulties.isVisible = false
            soundManager.playClickSound(1)
        } else {
            finishAffinity()
        }
    }

    companion object {
        val TAG = MainActivity::class.simpleName
    }
}
