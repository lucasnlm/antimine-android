package dev.lucasnlm.antimine

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev.lucasnlm.antimine.common.level.repository.SavesRepository
import dev.lucasnlm.antimine.common.level.view.GameRenderFragment
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.control.ControlActivity
import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.core.dpToPx
import dev.lucasnlm.antimine.core.isPortrait
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.serializableNonSafe
import dev.lucasnlm.antimine.databinding.ActivityGameBinding
import dev.lucasnlm.antimine.gameover.GameOverDialogFragment
import dev.lucasnlm.antimine.gameover.WinGameDialogFragment
import dev.lucasnlm.antimine.gameover.model.CommonDialogState
import dev.lucasnlm.antimine.gameover.model.GameResult
import dev.lucasnlm.antimine.gdx.GameContext
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.tutorial.TutorialActivity
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.ui.ext.showWarning
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.external.AdsManager
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.FeatureFlagManager
import dev.lucasnlm.external.InstantAppManager
import dev.lucasnlm.external.PlayGamesManager
import dev.lucasnlm.external.ReviewWrapperImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit
import dev.lucasnlm.antimine.i18n.R as i18n
import dev.lucasnlm.antimine.ui.R as ui

class GameActivity :
    ThemedActivity(),
    AndroidFragmentApplication.Callbacks {

    private val gameViewModel by viewModel<GameViewModel>()
    private val appScope: CoroutineScope by inject()
    private val preferencesRepository: PreferencesRepository by inject()
    private val analyticsManager: AnalyticsManager by inject()
    private val instantAppManager: InstantAppManager by inject()
    private val savesRepository: SavesRepository by inject()
    private val playGamesManager: PlayGamesManager by inject()
    private val gameAudioManager: GameAudioManager by inject()
    private val adsManager: AdsManager by inject()
    private val reviewWrapper: ReviewWrapperImpl by inject()
    private val featureFlagManager: FeatureFlagManager by inject()
    private val cloudSaveManager by inject<CloudSaveManager>()

    private var warning: Snackbar? = null
    private var revealBombFeedback: ValueAnimator? = null

    private val hasFloatingButton = preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen
    private val binding: ActivityGameBinding by lazy {
        ActivityGameBinding.inflate(layoutInflater)
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.run(::handleIntent)

        GameContext.zoom = 1.0f
        GameContext.zoomLevelAlpha = 1.0f
    }

    private fun Int.toL10nString() = String.format("%d", this)

    private fun showGameWarning(
        @StringRes text: Int,
    ) {
        val isSwitchAndOpen = preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen
        warning?.dismiss()
        warning = showWarning(text, isSwitchAndOpen)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        if (!preferencesRepository.isPremiumEnabled()) {
            adsManager.start(this)
        }

        bindViewModel()
        bindToolbar()
        loadGameOrTutorial()
        bindTapToBegin()

        playGamesManager.showPlayPopUp(this)
        playGamesStartUp()

        onBackPressedDispatcher.addCallback {
            finish()
        }
    }

    private fun handleIntent(intent: Intent) {
        lifecycleScope.launch {
            val extras = intent.extras ?: Bundle()
            val queryParamDifficulty = intent.data?.getQueryParameter("difficulty")
            when {
                queryParamDifficulty != null -> {
                    val upperDifficulty = queryParamDifficulty.uppercase()
                    val difficulty = Difficulty.values().firstOrNull { it.id == upperDifficulty }
                    if (difficulty == null) {
                        gameViewModel.loadLastGame()
                    } else {
                        gameViewModel.startNewGame(difficulty)
                    }
                }
                extras.containsKey(DIFFICULTY) -> {
                    intent.removeExtra(DIFFICULTY)
                    val difficulty = extras.serializableNonSafe<Difficulty>(DIFFICULTY)
                    gameViewModel.startNewGame(difficulty)
                }
                extras.containsKey(RETRY_GAME) -> {
                    val uid = extras.getInt(RETRY_GAME)
                    gameViewModel.retryGame(uid)
                }
                extras.containsKey(START_GAME) -> {
                    val uid = extras.getInt(START_GAME)
                    gameViewModel.loadGame(uid)
                }
                else -> {
                    gameViewModel.loadLastGame()
                }
            }
        }
    }

    private fun playGamesStartUp() {
        if (playGamesManager.hasGooglePlayGames() && playGamesManager.shouldRequestLogin()) {
            playGamesManager.keepRequestingLogin(false)
            lifecycleScope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        val logged = playGamesManager.silentLogin()
                        if (!logged) {
                            preferencesRepository.setUserId("")
                        }
                        playGamesManager.showPlayPopUp(this@GameActivity)
                    }
                }.onFailure {
                    Log.e(TAG, "Failed silent login", it)
                }
            }
        }
    }

    private fun startCountAnimation(
        from: Int,
        to: Int,
        updateMineCount: (Int) -> Unit,
    ) {
        ValueAnimator.ofInt(from, to).apply {
            duration = MINE_COUNTER_ANIM_COUNTER_MS
            addUpdateListener { animation ->
                updateMineCount(animation.animatedValue as Int)
            }
        }.start()
    }

    private fun bindViewModel() =
        gameViewModel.apply {
            lifecycleScope.launch {
                observeState().collect {
                    if (it.isNewGame) {
                        warning?.dismiss()
                        warning = null

                        withContext(Dispatchers.Main) {
                            stopKonfettiView()
                        }

                        val color = usingTheme.palette.covered.toAndroidColor(168)
                        val tint = ColorStateList.valueOf(color)

                        binding.tapToBegin.apply {
                            text =
                                when {
                                    it.isCreatingGame -> {
                                        getString(i18n.string.creating_valid_game)
                                    }
                                    it.isLoadingMap -> {
                                        getString(i18n.string.loading)
                                    }
                                    else -> {
                                        getString(i18n.string.tap_to_begin)
                                    }
                                }
                            isVisible = true
                            backgroundTintList = tint
                        }
                    } else {
                        binding.tapToBegin.isVisible = false
                    }

                    if (it.isGameStarted && it.isActive) {
                        gameAudioManager.playMusic()
                    }

                    if (it.isCreatingGame) {
                        launch {
                            // Show loading indicator only when it takes more than:
                            delay(LOADING_INDICATOR_MS)
                            if (singleState().isCreatingGame) {
                                binding.loadingGame.show()
                            }
                        }
                    } else if (binding.loadingGame.isVisible) {
                        binding.loadingGame.hide()
                    }

                    if (it.shouldShowControls) {
                        val color = usingTheme.palette.covered.toAndroidColor(168)
                        val tint = ColorStateList.valueOf(color)
                        val controlText = gameViewModel.getControlDescription(applicationContext)

                        if (!controlText.isNullOrBlank()) {
                            binding.controlsToast.apply {
                                isVisible = true
                                backgroundTintList = tint
                                text = controlText

                                setOnClickListener {
                                    val intent = Intent(this@GameActivity, ControlActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        } else {
                            binding.controlsToast.isVisible = false
                        }
                    } else {
                        binding.controlsToast.isVisible = false
                    }

                    binding.timer.apply {
                        isVisible = preferencesRepository.showTimer() && it.duration != 0L
                        text = DateUtils.formatElapsedTime(it.duration)
                    }

                    binding.minesCount.apply {
                        val currentMineCount = it.mineCount
                        if (currentMineCount != null) {
                            val oldValue = text.toString().toIntOrNull()
                            if (oldValue != null) {
                                if (currentMineCount < 0) {
                                    if (oldValue > currentMineCount) {
                                        startAnimation(AnimationUtils.loadAnimation(context, R.anim.fast_shake))
                                    }
                                }

                                startCountAnimation(oldValue, currentMineCount) { animateIt ->
                                    text = animateIt.toL10nString()
                                }
                            } else {
                                text = currentMineCount.toL10nString()
                            }

                            isVisible = true
                        } else {
                            isVisible = false
                        }
                    }

                    binding.hintCounter.text = it.hints.toL10nString()

                    if (!it.isGameCompleted && it.isActive && it.useHelp) {
                        refreshTipShortcutIcon()
                    } else {
                        refreshRetryShortcut(it.hasMines)
                    }

                    keepScreenOn(it.isActive)
                }
            }

            lifecycleScope.launch {
                gameViewModel.observeSideEffects().collect {
                    when (it) {
                        is GameEvent.ShowNoGuessFailWarning -> {
                            warning =
                                showWarning(i18n.string.no_guess_fail_warning).apply {
                                    setAction(i18n.string.ok) {
                                        warning?.dismiss()
                                    }
                                    show()
                                }
                        }
                        is GameEvent.ShowNewGameDialog -> {
                            lifecycleScope.launch {
                                GameOverDialogFragment.newInstance(
                                    CommonDialogState(
                                        gameResult = GameResult.Completed,
                                        showContinueButton = gameViewModel.hasUnknownMines(),
                                        rightMines = 0,
                                        totalMines = 0,
                                        time = singleState().duration,
                                        received = 0,
                                        turn = -1,
                                    ),
                                ).run {
                                    showAllowingStateLoss(supportFragmentManager, WinGameDialogFragment.TAG)
                                }
                            }
                        }
                        is GameEvent.VictoryDialog -> {
                            if (preferencesRepository.showWindowsWhenFinishGame()) {
                                withContext(Dispatchers.Main) {
                                    showKonfettiView()
                                }

                                lifecycleScope.launch {
                                    delay(it.delayToShow)

                                    gameAudioManager.pauseMusic()

                                    WinGameDialogFragment.newInstance(
                                        CommonDialogState(
                                            gameResult = GameResult.Victory,
                                            showContinueButton = false,
                                            rightMines = it.rightMines,
                                            totalMines = it.totalMines,
                                            time = it.timestamp,
                                            received = it.receivedTips,
                                            turn = -1,
                                        ),
                                    ).run {
                                        showAllowingStateLoss(supportFragmentManager, WinGameDialogFragment.TAG)

                                        dialog?.setOnDismissListener {
                                            if (!isFinishing) {
                                                reviewWrapper.startInAppReview(this@GameActivity)
                                            }
                                        }
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    showKonfettiView()
                                }
                                gameAudioManager.pauseMusic()
                                showEndGameToast(GameResult.Victory)
                            }
                        }
                        is GameEvent.GameOverDialog -> {
                            if (preferencesRepository.showWindowsWhenFinishGame()) {
                                lifecycleScope.launch {
                                    delay(it.delayToShow)
                                    GameOverDialogFragment.newInstance(
                                        CommonDialogState(
                                            gameResult = GameResult.GameOver,
                                            showContinueButton = gameViewModel.hasUnknownMines(),
                                            rightMines = it.rightMines,
                                            totalMines = it.totalMines,
                                            time = it.timestamp,
                                            received = it.receivedTips,
                                            turn = it.turn,
                                        ),
                                    ).run {
                                        showAllowingStateLoss(supportFragmentManager, WinGameDialogFragment.TAG)
                                    }
                                }
                            } else {
                                showEndGameToast(GameResult.GameOver)
                            }
                        }
                        is GameEvent.GameCompleteDialog -> {
                            if (preferencesRepository.showWindowsWhenFinishGame()) {
                                lifecycleScope.launch {
                                    delay(it.delayToShow)
                                    GameOverDialogFragment.newInstance(
                                        CommonDialogState(
                                            gameResult = GameResult.Completed,
                                            showContinueButton = false,
                                            rightMines = it.rightMines,
                                            totalMines = it.totalMines,
                                            time = it.timestamp,
                                            received = it.receivedTips,
                                            turn = it.turn,
                                        ),
                                    ).run {
                                        showAllowingStateLoss(supportFragmentManager, WinGameDialogFragment.TAG)

                                        dialog?.setOnDismissListener {
                                            if (!isFinishing) {
                                                reviewWrapper.startInAppReview(this@GameActivity)
                                            }
                                        }
                                    }
                                }
                            } else {
                                showEndGameToast(GameResult.Completed)
                            }
                        }
                        else -> {
                            // Empty
                        }
                    }
                }
            }
        }

    override fun onResume() {
        super.onResume()
        if (hasFloatingButton != (preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen)
        ) {
            // If used changed any currently rendered settings, we
            // must recreate the activity to force all sprites are updated.
            recreate()
            return
        }

        analyticsManager.sentEvent(Analytics.Resume)
        keepScreenOn(true)
        gameViewModel.resumeGame()

        if (gameViewModel.singleState().isActive) {
            gameAudioManager.resumeMusic()
        }
    }

    override fun onPause() {
        super.onPause()
        keepScreenOn(false)

        revealBombFeedback?.removeAllListeners()
        revealBombFeedback = null

        cloudSaveManager.uploadSave()

        if (isFinishing) {
            analyticsManager.sentEvent(Analytics.Quit)
        } else if (gameViewModel.singleState().isActive) {
            gameViewModel.pauseGame()
        }

        appScope.launch {
            gameViewModel.saveGame()
        }

        gameAudioManager.pauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameAudioManager.stopMusic()
    }

    private fun bindTapToBegin() {
        binding.tapToBegin.apply {
            setTextColor(usingTheme.palette.background.toAndroidColor())
        }

        if (preferencesRepository.showTutorialButton()) {
            binding.controlsToast.apply {
                setTextColor(usingTheme.palette.background.toAndroidColor())
            }
        }
    }

    private fun bindToolbar() {
        binding.back.apply {
            TooltipCompat.setTooltipText(this, getString(i18n.string.back))
            setColorFilter(binding.minesCount.currentTextColor)
            setOnClickListener {
                onBackPressed()
            }
        }

        binding.appBar.apply {
            setBackgroundColor(usingTheme.palette.background.toAndroidColor(200))
        }

        if (applicationContext.isPortrait()) {
            binding.minesCount.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this, ui.drawable.mine),
                null,
                null,
                null,
            )
        } else {
            binding.minesCount.setCompoundDrawablesWithIntrinsicBounds(
                null,
                ContextCompat.getDrawable(this, ui.drawable.mine),
                null,
                null,
            )
        }
    }

    private fun refreshTipShortcutIcon() {
        val dt = System.currentTimeMillis() - preferencesRepository.lastHelpUsed()
        val canUseHelpNow = dt > TIP_COOLDOWN_MS
        val canRequestHelpWithAds = gameViewModel.getTips() == 0 && adsManager.isAvailable()

        binding.hintCounter.apply {
            isVisible = canUseHelpNow
            text =
                if (canRequestHelpWithAds) {
                    "+5"
                } else {
                    gameViewModel.getTips().toL10nString()
                }
        }

        binding.shortcutIcon.apply {
            TooltipCompat.setTooltipText(this, getString(i18n.string.help))
            setImageResource(R.drawable.hint)
            setColorFilter(binding.minesCount.currentTextColor)

            if (canUseHelpNow) {
                binding.hintCooldown.apply {
                    animate().alpha(0.0f).start()
                    isVisible = false
                    progress = 0
                }

                animate().alpha(1.0f).start()

                if (canRequestHelpWithAds) {
                    setOnClickListener {
                        lifecycleScope.launch {
                            analyticsManager.sentEvent(Analytics.RequestMoreHints)
                            val wasPlaying = gameAudioManager.isPlayingMusic()
                            adsManager.showRewardedAd(
                                activity = this@GameActivity,
                                skipIfFrequent = false,
                                onStart = {
                                    if (wasPlaying) {
                                        gameAudioManager.pauseMusic()
                                    }
                                },
                                onRewarded = {
                                    if (wasPlaying) {
                                        gameAudioManager.resumeMusic()
                                    }
                                    gameViewModel.revealRandomMine(false)
                                    gameViewModel.sendEvent(GameEvent.GiveMoreTip)
                                },
                                onFail = {
                                    if (wasPlaying) {
                                        gameAudioManager.resumeMusic()
                                    }
                                    showGameWarning(i18n.string.fail_to_load_ad)
                                },
                            )
                        }
                    }
                } else {
                    setOnClickListener {
                        lifecycleScope.launch {
                            revealRandomMine()
                        }
                    }
                }
            } else {
                binding.hintCooldown.apply {
                    animate().alpha(1.0f).start()
                    if (progress == 0) {
                        ValueAnimator.ofInt(0, TIP_COOLDOWN_MS.toInt()).apply {
                            duration = TIP_COOLDOWN_MS
                            repeatCount = 0
                            addUpdateListener {
                                progress = it.animatedValue as Int
                            }
                            revealBombFeedback = this

                            addListener(
                                object : Animator.AnimatorListener {
                                    override fun onAnimationStart(animation: Animator) {
                                        // Ignore
                                    }

                                    override fun onAnimationEnd(animation: Animator) {
                                        if (!isPaused) {
                                            gameAudioManager.playRevealBombReloaded()
                                        }
                                    }

                                    override fun onAnimationCancel(animation: Animator) {
                                        // Ignore
                                    }

                                    override fun onAnimationRepeat(animation: Animator) {
                                        // Ignore
                                    }
                                },
                            )
                            start()
                        }
                    }
                    isVisible = true
                    max = TIP_COOLDOWN_MS.toInt()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        setProgress(dt.toInt(), true)
                    } else {
                        progress = dt.toInt()
                    }
                }

                animate().alpha(0.0f).start()

                setOnClickListener {
                    showGameWarning(i18n.string.cant_do_it_now)
                }
            }
        }
    }

    private fun revealRandomMine() {
        analyticsManager.sentEvent(Analytics.UseHint)

        val hintAmount = gameViewModel.getTips()
        if (hintAmount > 0) {
            val revealedId = gameViewModel.revealRandomMine()
            if (revealedId == null) {
                showGameWarning(i18n.string.cant_do_it_now)
            } else {
                showGameWarning(i18n.string.mine_revealed)
            }
        } else {
            showGameWarning(i18n.string.help_win_a_game)
        }
    }

    private fun startNewGameWithAds() {
        if (!preferencesRepository.isPremiumEnabled() && featureFlagManager.isAdsOnNewGameEnabled) {
            if (featureFlagManager.useInterstitialAd) {
                adsManager.showInterstitialAd(
                    activity = this,
                    onDismiss = {
                        lifecycleScope.launch {
                            gameViewModel.startNewGame()
                        }
                    },
                )
            } else {
                adsManager.showRewardedAd(
                    activity = this,
                    skipIfFrequent = true,
                    onRewarded = {
                        lifecycleScope.launch {
                            gameViewModel.startNewGame()
                        }
                    },
                    onFail = {
                        lifecycleScope.launch {
                            gameViewModel.startNewGame()
                        }
                    },
                )
            }
        } else {
            lifecycleScope.launch {
                gameViewModel.startNewGame()
            }
        }
    }

    private fun refreshRetryShortcut(enabled: Boolean) {
        binding.shortcutIcon.apply {
            TooltipCompat.setTooltipText(this, getString(i18n.string.new_game))
            setImageResource(R.drawable.retry)
            setColorFilter(binding.minesCount.currentTextColor)
            setOnClickListener {
                lifecycleScope.launch {
                    gameAudioManager.playClickSound()
                    val confirmResign = gameViewModel.singleState().isActive
                    analyticsManager.sentEvent(Analytics.TapGameReset(confirmResign))

                    if (confirmResign) {
                        newGameConfirmation {
                            startNewGameWithAds()
                        }
                    } else {
                        startNewGameWithAds()
                    }
                }
            }
        }

        binding.hintCounter.isVisible = false

        binding.shortcutIcon.apply {
            isClickable = enabled
            val alphaValue =
                if (enabled) {
                    ENABLED_SHORTCUT_ALPHA
                } else {
                    DISABLED_SHORTCUT_ALPHA
                }
            animate().alpha(alphaValue).start()
        }
    }

    private fun onOpenAppActions() {
        if (instantAppManager.isEnabled(applicationContext)) {
            // Instant App does nothing.
            savesRepository.setLimit(1)
        } else {
            preferencesRepository.incrementUseCount()

            if (preferencesRepository.getUseCount() > featureFlagManager.minUsageToReview) {
                reviewWrapper.startInAppReview(this)
            }
        }

        lifecycleScope.launch {
            if (preferencesRepository.showTutorialDialog()) {
                val firstLocale = ConfigurationCompat.getLocales(resources.configuration).get(0)
                val lang = firstLocale?.language

                val message = getString(i18n.string.do_you_know_how_to_play)
                val baseText = "Do you know how to play minesweeper?"

                if (lang != null && (lang == "en" || message != baseText)) {
                    MaterialAlertDialogBuilder(this@GameActivity).run {
                        setTitle(i18n.string.tutorial)
                        setMessage(i18n.string.do_you_know_how_to_play)
                        setPositiveButton(i18n.string.open_tutorial) { _, _ ->
                            analyticsManager.sentEvent(Analytics.KnowHowToPlay(false))
                            preferencesRepository.setTutorialDialog(false)
                            val intent = Intent(this@GameActivity, TutorialActivity::class.java)
                            startActivity(intent)
                        }
                        setNegativeButton(i18n.string.close) { _, _ ->
                            analyticsManager.sentEvent(Analytics.KnowHowToPlay(true))
                            preferencesRepository.setTutorialDialog(false)
                        }
                        show()
                    }
                }
            }
        }
    }

    private fun loadGameOrTutorial() {
        if (!isFinishing) {
            lifecycleScope.launch {
                loadGameFragment()
            }
        }
    }

    private suspend fun loadGameFragment() {
        supportFragmentManager.apply {
            if (findFragmentByTag(GameRenderFragment.TAG) == null) {
                val fragment =
                    withContext(Dispatchers.IO) {
                        GameRenderFragment()
                    }

                withContext(Dispatchers.Main) {
                    beginTransaction().apply {
                        replace(R.id.levelContainer, fragment, GameRenderFragment.TAG)
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        commitAllowingStateLoss()
                    }
                }

                handleIntent(intent)
                onOpenAppActions()
            }
        }
    }

    private fun newGameConfirmation(action: () -> Unit) {
        MaterialAlertDialogBuilder(this).apply {
            setTitle(i18n.string.new_game)
            setMessage(i18n.string.retry_sure)
            setPositiveButton(i18n.string.resume) { _, _ -> action() }
            setNegativeButton(i18n.string.cancel, null)
            show()
        }
    }

    private fun showEndGameToast(gameResult: GameResult) {
        warning?.dismiss()

        val message =
            when (gameResult) {
                GameResult.GameOver -> i18n.string.you_lost
                GameResult.Victory -> i18n.string.you_won
                GameResult.Completed -> i18n.string.you_finished
            }

        warning =
            Snackbar.make(
                binding.root,
                message,
                Snackbar.LENGTH_LONG,
            ).apply {
                if (preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen) {
                    view.translationY = -dpToPx(TOAST_OFFSET_Y_DP).toFloat()
                }
                show()
            }
    }

    private fun keepScreenOn(enabled: Boolean) {
        window.run {
            if (enabled) {
                addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    private fun stopKonfettiView() {
        binding.konfettiView?.stopGracefully()
    }

    private fun showKonfettiView() {
        binding.konfettiView?.apply {
            start(
                Party(
                    speed = 0f,
                    maxSpeed = 30f,
                    damping = 0.9f,
                    spread = 360,
                    colors = CONFETTI_COLORS,
                    emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(MAX_CONFETTI_COUNT),
                    position = CONFETTI_POSITION,
                ),
            )
        }
    }

    override fun exit() {
        // LibGDX exit callback
    }

    companion object {
        val TAG = GameActivity::class.simpleName

        const val DIFFICULTY = "difficulty"
        const val START_GAME = "start_game"
        const val RETRY_GAME = "retry_game"

        const val TIP_COOLDOWN_MS = 5 * 1000L
        const val MINE_COUNTER_ANIM_COUNTER_MS = 250L
        const val LOADING_INDICATOR_MS = 500L

        const val MAX_CONFETTI_COUNT = 100
        val CONFETTI_COLORS = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def)
        val CONFETTI_POSITION = Position.Relative(0.5, 0.2)

        const val TOAST_OFFSET_Y_DP = 128

        const val ENABLED_SHORTCUT_ALPHA = 1.0f
        const val DISABLED_SHORTCUT_ALPHA = 0.3f
    }
}
