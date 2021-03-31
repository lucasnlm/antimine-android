package dev.lucasnlm.antimine

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.core.isPortrait
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.gameover.GameOverDialogFragment
import dev.lucasnlm.antimine.gameover.WinGameDialogFragment
import dev.lucasnlm.antimine.gameover.model.GameResult
import dev.lucasnlm.antimine.level.view.GdxLevelFragment
import dev.lucasnlm.antimine.main.MainActivity
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.share.ShareManager
import dev.lucasnlm.antimine.splash.SplashActivity
import dev.lucasnlm.antimine.tutorial.view.TutorialLevelFragment
import dev.lucasnlm.antimine.ui.ThematicActivity
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.external.IAdsManager
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IFeatureFlagManager
import dev.lucasnlm.external.IInstantAppManager
import dev.lucasnlm.external.IPlayGamesManager
import dev.lucasnlm.external.ReviewWrapper
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tutorial_level.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
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
    private val shareViewModel: ShareManager by inject()
    private val playGamesManager: IPlayGamesManager by inject()
    private val adsManager: IAdsManager by inject()
    private val reviewWrapper: ReviewWrapper by inject()
    private val featureFlagManager: IFeatureFlagManager by inject()
    private val cloudSaveManager by inject<CloudSaveManager>()
    private var gameToast: Toast? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.run(::handleIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        if (!preferencesRepository.isPremiumEnabled()) {
            adsManager.start(this)
        }

        bindViewModel()
        bindToolbar()
        loadGameOrTutorial()
        handleIntent(intent)
        bindTapToBegin()

        if (!isPortrait()) {
            val decorView = window.decorView

            @Suppress("DEPRECATION")
            val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN

            @Suppress("DEPRECATION")
            decorView.systemUiVisibility = uiOptions
        }

        playGamesManager.showPlayPopUp(this)

        onOpenAppActions()
        playGamesStartUp()
    }

    private fun handleIntent(intent: Intent) {
        lifecycleScope.launch {
            val extras = intent.extras ?: Bundle()
            when {
                extras.containsKey(DIFFICULTY) -> {
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
        if (playGamesManager.hasGooglePlayGames()) {
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

    private fun bindViewModel() = gameViewModel.apply {
        lifecycleScope.launchWhenCreated {
            observeState().collect {
                if (it.turn == 0 && it.saveId == 0L) {
                    val color = usingTheme.palette.covered.toAndroidColor(168)
                    val tint = ColorStateList.valueOf(color)
                    tapToBegin.apply {
                        visibility = View.VISIBLE
                        backgroundTintList = tint
                    }
                } else {
                    tapToBegin.visibility = View.GONE
                }

                if (it.turn < 3 && it.saveId == 0L) {
                    val color = usingTheme.palette.covered.toAndroidColor(168)
                    val tint = ColorStateList.valueOf(color)
                    gameViewModel.getControlDescription(applicationContext)?.let { text ->
                        controlsToast.apply {
                            visibility = View.VISIBLE
                            backgroundTintList = tint
                            this.text = text
                        }
                    }
                } else {
                    controlsToast.visibility = View.GONE
                }

                timer.apply {
                    visibility = if (it.duration == 0L) View.GONE else View.VISIBLE
                    text = DateUtils.formatElapsedTime(it.duration)
                }

                minesCount.apply {
                    if (it.mineCount < 0) {
                        text.toString().toIntOrNull()?.let { oldValue ->
                            if (oldValue > it.mineCount) {
                                startAnimation(AnimationUtils.loadAnimation(context, R.anim.fast_shake))
                            }
                        }
                    }

                    visibility = View.VISIBLE
                    text = it.mineCount.toString()
                }

                tipsCounter.text = it.tips.toString()

                if (!it.isGameCompleted && it.useHelp) {
                    refreshTipShortcutIcon()
                } else {
                    refreshRetryShortcut(it.hasMines)
                }

                if (it.isActive) {
                    gameToast?.cancel()
                }

                keepScreenOn(it.isActive)
            }
        }

        lifecycleScope.launchWhenCreated {
            gameViewModel.observeSideEffects().collect {
                when (it) {
                    is GameEvent.ShareGame -> {
                        shareCurrentGame()
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
                                    showContinueButton = true,
                                    rightMines = it.rightMines,
                                    totalMines = it.totalMines,
                                    time = it.timestamp,
                                    received = it.receivedTips,
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
                                ).run {
                                    showAllowingStateLoss(supportFragmentManager, WinGameDialogFragment.TAG)
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
    }

    private fun backToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }

    override fun onBackPressed() {
        backToMainActivity()
    }

    private fun bindTapToBegin() {
        tapToBegin.apply {
            setTextColor(usingTheme.palette.background.toAndroidColor(255))
        }
        controlsToast.apply {
            setTextColor(usingTheme.palette.background.toAndroidColor(255))
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
                ContextCompat.getDrawable(this, usingTheme.assets.toolbarMine),
                null,
                null,
                null,
            )
        } else {
            minesCount.setCompoundDrawablesWithIntrinsicBounds(
                null,
                ContextCompat.getDrawable(this, usingTheme.assets.toolbarMine),
                null,
                null,
            )
        }
    }

    private fun refreshTipShortcutIcon() {
        tipsCounter.apply {
            visibility = View.VISIBLE
            text = gameViewModel.getTips().toString()
        }

        shortcutIcon.apply {
            TooltipCompat.setTooltipText(this, getString(R.string.help))
            setImageResource(R.drawable.tip)
            setColorFilter(minesCount.currentTextColor)
            visibility = View.VISIBLE
            animate().alpha(1.0f).start()
            setOnClickListener {
                lifecycleScope.launch {
                    analyticsManager.sentEvent(Analytics.UseTip)

                    if (gameViewModel.getTips() > 0) {
                        if (!gameViewModel.revealRandomMine()) {
                            Toast.makeText(applicationContext, R.string.cant_do_it_now, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, R.string.help_win_a_game, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun startNewGameWithAds() {
        if (!preferencesRepository.isPremiumEnabled() && featureFlagManager.isAdsOnNewGameEnabled) {
            adsManager.showRewardedAd(
                activity = this,
                skipIfFrequent = true,
                onRewarded = {
                    gameViewModel.startNewGame()
                }
            )
        } else {
            gameViewModel.startNewGame()
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
    }

    private fun loadGameOrTutorial() {
        if (!isFinishing) {
            loadGameFragment()
        }
    }

    private fun loadGameFragment() {
        supportFragmentManager.apply {
            findFragmentByTag(TutorialLevelFragment.TAG)?.let { it ->
                beginTransaction().apply {
                    remove(it)
                    commitAllowingStateLoss()
                }
            }

            if (findFragmentByTag(GdxLevelFragment.TAG) == null) {
                beginTransaction().apply {
                    replace(R.id.levelContainer, GdxLevelFragment(), GdxLevelFragment.TAG)
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    commitAllowingStateLoss()
                }
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
        gameToast?.cancel()

        val message = when (gameResult) {
            GameResult.GameOver -> R.string.you_lost
            GameResult.Victory -> R.string.you_won
            GameResult.Completed -> R.string.you_finished
        }

        gameToast = Toast.makeText(this, message, Toast.LENGTH_LONG).apply {
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
    }

    private fun shareCurrentGame() {
        val levelSetup = gameViewModel.singleState().minefield
        val field = gameViewModel.singleState().field
        lifecycleScope.launch {
            shareViewModel.shareField(levelSetup, field)
        }
    }

    private fun keepScreenOn(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
