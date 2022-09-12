package dev.lucasnlm.antimine

import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.annotation.StringRes
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.view.GdxLevelFragment
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.control.ControlActivity
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.core.isPortrait
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.gameover.GameOverDialogFragment
import dev.lucasnlm.antimine.gameover.WinGameDialogFragment
import dev.lucasnlm.antimine.gameover.model.GameResult
import dev.lucasnlm.antimine.gdx.GdxLocal
import dev.lucasnlm.antimine.main.MainActivity
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.splash.SplashActivity
import dev.lucasnlm.antimine.tutorial.TutorialActivity
import dev.lucasnlm.antimine.ui.ext.ThematicActivity
import dev.lucasnlm.antimine.ui.ext.showWarning
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.external.IAdsManager
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IFeatureFlagManager
import dev.lucasnlm.external.IInstantAppManager
import dev.lucasnlm.external.IPlayGamesManager
import dev.lucasnlm.external.ReviewWrapper
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameActivity :
    ThematicActivity(R.layout.activity_game),
    AndroidFragmentApplication.Callbacks {

    private val gameViewModel by viewModel<GameViewModel>()
    private val preferencesRepository: IPreferencesRepository by inject()
    private val analyticsManager: IAnalyticsManager by inject()
    private val instantAppManager: IInstantAppManager by inject()
    private val savesRepository: ISavesRepository by inject()
    private val playGamesManager: IPlayGamesManager by inject()
    private val adsManager: IAdsManager by inject()
    private val reviewWrapper: ReviewWrapper by inject()
    private val featureFlagManager: IFeatureFlagManager by inject()
    private val cloudSaveManager by inject<CloudSaveManager>()
    private var warning: Snackbar? = null

    private val renderSquareRadius = preferencesRepository.squareRadius()
    private val renderSquareDivider = preferencesRepository.squareDivider()
    private val renderSquareSize = preferencesRepository.squareSize()
    private val showFloatingButtonOnTop = preferencesRepository.showToggleButtonOnTopBar()
    private val hasFloatingButton = preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.run(::handleIntent)

        GdxLocal.zoom = 1.0f
        GdxLocal.zoomLevelAlpha = 1.0f
    }

    private fun Int.toL10nString() = String.format("%d", this)

    private fun showGameWarning(@StringRes text: Int) {
        warning?.dismiss()
        warning = showWarning(text)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!preferencesRepository.isPremiumEnabled()) {
            adsManager.start(this)
        }

        bindViewModel()
        bindToolbar()
        loadGameOrTutorial()
        bindTapToBegin()
        bindToggleButton()

        playGamesManager.showPlayPopUp(this)
        playGamesStartUp()
    }

    private fun handleIntent(intent: Intent) {
        lifecycleScope.launch {
            val extras = intent.extras ?: Bundle()
            when {
                extras.containsKey(DIFFICULTY) -> {
                    intent.removeExtra(DIFFICULTY)
                    val difficulty = extras.getSerializable(DIFFICULTY) as Difficulty
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
            lifecycleScope.launchWhenCreated {
                try {
                    withContext(Dispatchers.IO) {
                        val logged = playGamesManager.silentLogin()
                        if (!logged) {
                            preferencesRepository.setUserId("")
                        }
                        playGamesManager.showPlayPopUp(this@GameActivity)
                    }
                } catch (e: Exception) {
                    Log.e(SplashActivity.TAG, "Failed silent login", e)
                }
            }
        }
    }

    private fun startCountAnimation(from: Int, to: Int, updateMineCount: (Int) -> Unit) {
        ValueAnimator.ofInt(from, to).apply {
            duration = 250
            addUpdateListener { animation ->
                updateMineCount(animation.animatedValue as Int)
            }
        }.start()
    }

    private fun bindViewModel() = gameViewModel.apply {
        lifecycleScope.launchWhenCreated {
            observeState().collect {
                if (it.turn == 0 && it.saveId == 0L || it.isLoadingMap) {
                    if (it.turn == 0) {
                        setOpenControlSwitcherIcon()
                        warning?.dismiss()
                        warning = null
                    }

                    val color = usingTheme.palette.covered.toAndroidColor(168)
                    val tint = ColorStateList.valueOf(color)

                    tapToBegin.apply {
                        text = when {
                            it.isLoadingMap -> {
                                getString(R.string.loading)
                            }
                            else -> {
                                getString(R.string.tap_to_begin)
                            }
                        }
                        visibility = View.VISIBLE
                        backgroundTintList = tint
                    }
                } else {
                    tapToBegin.visibility = View.GONE
                }

                if (it.turn < 1 && it.saveId == 0L && !it.isLoadingMap && it.showTutorial) {
                    val color = usingTheme.palette.covered.toAndroidColor(168)
                    val tint = ColorStateList.valueOf(color)
                    val controlText = gameViewModel.getControlDescription(applicationContext)

                    if (controlText != null && controlText.isNotBlank()) {
                        controlsToast.apply {
                            visibility = View.VISIBLE
                            backgroundTintList = tint
                            text = controlText

                            setOnClickListener {
                                val intent = Intent(this@GameActivity, ControlActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    } else {
                        controlsToast.visibility = View.GONE
                    }
                } else {
                    controlsToast.visibility = View.GONE
                }

                timer.apply {
                    visibility = if (it.duration == 0L) View.GONE else View.VISIBLE
                    text = DateUtils.formatElapsedTime(it.duration)
                }

                minesCount.apply {
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

                        visibility = View.VISIBLE
                    } else {
                        visibility = View.GONE
                    }
                }

                tipsCounter.text = it.tips.toL10nString()

                if (!it.isGameCompleted && it.isActive && it.useHelp) {
                    refreshTipShortcutIcon()
                } else {
                    refreshRetryShortcut(it.hasMines)
                }

                keepScreenOn(it.isActive)
            }
        }

        lifecycleScope.launchWhenCreated {
            gameViewModel.observeSideEffects().collect {
                when (it) {
                    is GameEvent.ShowNoGuessFailWarning -> {
                        warning = Snackbar.make(
                            findViewById(android.R.id.content),
                            R.string.no_guess_fail_warning,
                            Snackbar.LENGTH_INDEFINITE,
                        ).apply {
                            setAction(R.string.ok) {
                                warning?.dismiss()
                            }
                            show()
                        }
                    }
                    is GameEvent.ShowNewGameDialog -> {
                        lifecycleScope.launch {
                            GameOverDialogFragment.newInstance(
                                gameResult = GameResult.Completed,
                                showContinueButton = gameViewModel.hasUnknownMines(),
                                rightMines = 0,
                                totalMines = 0,
                                time = singleState().duration,
                                received = 0,
                                turn = -1,
                            ).run {
                                showAllowingStateLoss(supportFragmentManager, WinGameDialogFragment.TAG)
                            }
                        }
                    }
                    is GameEvent.VictoryDialog -> {
                        if (preferencesRepository.showWindowsWhenFinishGame()) {
                            lifecycleScope.launch {
                                delay(it.delayToShow)
                                WinGameDialogFragment.newInstance(
                                    gameResult = GameResult.Victory,
                                    showContinueButton = false,
                                    rightMines = it.rightMines,
                                    totalMines = it.totalMines,
                                    time = it.timestamp,
                                    received = it.receivedTips,
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
                            showEndGameToast(GameResult.Victory)
                        }
                    }
                    is GameEvent.GameOverDialog -> {
                        if (preferencesRepository.showWindowsWhenFinishGame()) {
                            lifecycleScope.launch {
                                delay(it.delayToShow)
                                GameOverDialogFragment.newInstance(
                                    gameResult = GameResult.GameOver,
                                    showContinueButton = gameViewModel.hasUnknownMines(),
                                    rightMines = it.rightMines,
                                    totalMines = it.totalMines,
                                    time = it.timestamp,
                                    received = it.receivedTips,
                                    turn = it.turn,
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
                                    gameResult = GameResult.Completed,
                                    showContinueButton = false,
                                    rightMines = it.rightMines,
                                    totalMines = it.totalMines,
                                    time = it.timestamp,
                                    received = it.receivedTips,
                                    turn = it.turn,
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
        if (renderSquareRadius != preferencesRepository.squareRadius() ||
            renderSquareDivider != preferencesRepository.squareDivider() ||
            renderSquareSize != preferencesRepository.squareSize() ||
            showFloatingButtonOnTop != preferencesRepository.showToggleButtonOnTopBar() ||
            hasFloatingButton != (preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen)
        ) {
            // If used changed any currently rendered settings, we
            // must recreate the activity to force all sprites are updated.
            recreate()
            return
        }

        analyticsManager.sentEvent(Analytics.Resume)
        keepScreenOn(true)
        gameViewModel.resumeGame()
    }

    override fun onPause() {
        super.onPause()
        keepScreenOn(false)

        cloudSaveManager.uploadSave()

        if (isFinishing) {
            analyticsManager.sentEvent(Analytics.Quit)
        } else if (gameViewModel.singleState().isActive) {
            gameViewModel.pauseGame()
        }

        GlobalScope.launch {
            gameViewModel.saveGame()
        }
    }

    private fun backToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        backToMainActivity()
    }

    private fun bindTapToBegin() {
        tapToBegin.apply {
            setTextColor(usingTheme.palette.background.toAndroidColor(255))
        }
        if (preferencesRepository.showTutorialButton()) {
            controlsToast.apply {
                setTextColor(usingTheme.palette.background.toAndroidColor(255))
            }
        }
    }

    private fun bindToolbar() {
        back.apply {
            TooltipCompat.setTooltipText(this, getString(R.string.back))
            setColorFilter(minesCount.currentTextColor)
            setOnClickListener {
                backToMainActivity()
            }
        }

        app_bar?.apply {
            setBackgroundColor(usingTheme.palette.background.toAndroidColor(200))
        }

        if (applicationContext.isPortrait()) {
            minesCount.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this, R.drawable.mine),
                null,
                null,
                null,
            )
        } else {
            minesCount.setCompoundDrawablesWithIntrinsicBounds(
                null,
                ContextCompat.getDrawable(this, R.drawable.mine),
                null,
                null,
            )
        }
    }

    private fun refreshTipShortcutIcon() {
        val dt = System.currentTimeMillis() - preferencesRepository.lastHelpUsed()
        val canUseHelpNow = dt > 5 * 1000L
        val canRequestHelpWithAds = gameViewModel.getTips() == 0 && adsManager.isAvailable()

        tipsCounter.apply {
            visibility = if (canUseHelpNow) View.VISIBLE else View.GONE
            text = if (canRequestHelpWithAds) {
                "+5"
            } else {
                gameViewModel.getTips().toL10nString()
            }
        }

        shortcutIcon.apply {
            TooltipCompat.setTooltipText(this, getString(R.string.help))
            setImageResource(R.drawable.tip)
            setColorFilter(minesCount.currentTextColor)

            if (canUseHelpNow) {
                tipCooldown.apply {
                    animate().alpha(0.0f).start()
                    visibility = View.GONE
                    progress = 0
                }

                animate().alpha(1.0f).start()

                if (canRequestHelpWithAds) {
                    setOnClickListener {
                        lifecycleScope.launch {
                            analyticsManager.sentEvent(Analytics.RequestMoreTip)

                            adsManager.showRewardedAd(
                                activity = this@GameActivity,
                                skipIfFrequent = false,
                                onRewarded = {
                                    val value = 5
                                    gameViewModel.revealRandomMine(false)
                                    gameViewModel.sendEvent(GameEvent.GiveMoreTip(value))
                                },
                                onFail = {
                                    showGameWarning(R.string.cant_do_it_now)
                                },
                            )
                        }
                    }
                } else {
                    setOnClickListener {
                        lifecycleScope.launch {
                            analyticsManager.sentEvent(Analytics.UseTip)

                            if (gameViewModel.getTips() > 0) {
                                if (!gameViewModel.revealRandomMine()) {
                                    showGameWarning(R.string.cant_do_it_now)
                                } else {
                                    if (featureFlagManager.showAdWhenUsingTip) {
                                        if (!preferencesRepository.isPremiumEnabled()) {
                                            val state = gameViewModel.singleState()
                                            val adReward = (state.mineCount ?: 0) < state.minefield.mines * 0.10

                                            if (adReward) {
                                                adsManager.showRewardedAd(
                                                    this@GameActivity,
                                                    skipIfFrequent = false,
                                                    onRewarded = {},
                                                    onFail = {},
                                                )
                                            } else {
                                                adsManager.showInterstitialAd(
                                                    activity = this@GameActivity,
                                                    onDismiss = {},
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                showGameWarning(R.string.help_win_a_game)
                            }
                        }
                    }
                }
            } else {
                tipCooldown.apply {
                    animate().alpha(1.0f).start()
                    if (progress == 0) {
                        ValueAnimator.ofFloat(0.0f, 5.0f).apply {
                            duration = 5000
                            repeatCount = 0
                            addUpdateListener {
                                progress = ((it.animatedValue as Float) * 1000f).toInt()
                            }
                            start()
                        }
                    }
                    visibility = View.VISIBLE
                    max = 5000
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        setProgress(dt.toInt(), true)
                    } else {
                        progress = dt.toInt()
                    }
                }

                animate().alpha(0.0f).start()

                setOnClickListener {
                    showGameWarning(R.string.cant_do_it_now)
                }
            }
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
        shortcutIcon.apply {
            TooltipCompat.setTooltipText(this, getString(R.string.new_game))
            setImageResource(R.drawable.retry)
            setColorFilter(minesCount.currentTextColor)
            setOnClickListener {
                lifecycleScope.launch {
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

        tipsCounter.visibility = View.GONE
        shortcutIcon.apply {
            if (enabled) {
                isClickable = true
                animate().alpha(1.0f).start()
            } else {
                isClickable = false
                animate().alpha(0.3f).start()
            }
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

        lifecycleScope.launchWhenCreated {
            if (preferencesRepository.showTutorialDialog()) {
                val firstLocale = ConfigurationCompat.getLocales(resources.configuration).get(0)
                val lang = firstLocale?.language

                val message = getString(R.string.do_you_know_how_to_play)
                val baseText = "Do you know how to play minesweeper?"

                if (lang != null && (lang == "en" || (lang != "en" && message != baseText))) {
                    MaterialAlertDialogBuilder(this@GameActivity).run {
                        setTitle(R.string.tutorial)
                        setMessage(R.string.do_you_know_how_to_play)
                        setPositiveButton(R.string.open_tutorial) { _, _ ->
                            analyticsManager.sentEvent(Analytics.KnowHowToPlay(false))
                            preferencesRepository.setTutorialDialog(false)
                            val intent = Intent(this@GameActivity, TutorialActivity::class.java)
                            startActivity(intent)
                        }
                        setNegativeButton(R.string.close) { _, _ ->
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
            lifecycleScope.launchWhenCreated {
                loadGameFragment()
            }
        }
    }

    private suspend fun loadGameFragment() {
        supportFragmentManager.apply {
            if (findFragmentByTag(GdxLevelFragment.TAG) == null) {
                val fragment = withContext(Dispatchers.IO) {
                    GdxLevelFragment()
                }

                withContext(Dispatchers.Main) {
                    beginTransaction().apply {
                        replace(R.id.levelContainer, fragment, GdxLevelFragment.TAG)
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
            setTitle(R.string.new_game)
            setMessage(R.string.retry_sure)
            setPositiveButton(R.string.resume) { _, _ -> action() }
            setNegativeButton(R.string.cancel, null)
            show()
        }
    }

    private fun showEndGameToast(gameResult: GameResult) {
        warning?.dismiss()

        val message = when (gameResult) {
            GameResult.GameOver -> R.string.you_lost
            GameResult.Victory -> R.string.you_won
            GameResult.Completed -> R.string.you_finished
        }

        warning = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG,
        ).apply {
            show()
        }
    }

    private fun keepScreenOn(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun setOpenControlSwitcherIcon() {
        topBarToggle.apply {
            contentDescription = getString(R.string.open)
            TooltipCompat.setTooltipText(this, getString(R.string.open))
            setImageResource(R.drawable.touch)
            preferencesRepository.setSwitchControl(true)
            gameViewModel.refreshUseOpenOnSwitchControl(true)
        }
    }

    private fun setFlagControlSwitcherIcon() {
        topBarToggle.apply {
            contentDescription = getString(R.string.flag_tile)
            TooltipCompat.setTooltipText(this, getString(R.string.flag_tile))
            setImageResource(R.drawable.flag)
            preferencesRepository.setSwitchControl(false)
            gameViewModel.refreshUseOpenOnSwitchControl(false)
        }
    }

    private fun bindToggleButton() {
        if (preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen) {
            if (preferencesRepository.showToggleButtonOnTopBar()) {
                topBarToggle.apply {
                    visibility = View.VISIBLE
                    contentDescription = getString(R.string.open)
                    TooltipCompat.setTooltipText(this, getString(R.string.open))
                    setImageResource(R.drawable.touch)
                    preferencesRepository.setSwitchControl(true)

                    setOnClickListener {
                        if (preferencesRepository.openUsingSwitchControl()) {
                            setFlagControlSwitcherIcon()
                        } else {
                            setOpenControlSwitcherIcon()
                        }
                    }
                }
            } else {
                topBarToggle.visibility = View.GONE
            }

            gameViewModel.refreshUseOpenOnSwitchControl(true)
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
    }
}
