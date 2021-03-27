package dev.lucasnlm.antimine

import android.content.DialogInterface
import android.content.Intent
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
import androidx.lifecycle.Transformations
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.models.Status
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.core.isPortrait
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.models.Score
import dev.lucasnlm.antimine.gameover.GameOverDialogFragment
import dev.lucasnlm.antimine.gameover.WinGameDialogFragment
import dev.lucasnlm.antimine.gameover.model.GameResult
import dev.lucasnlm.antimine.level.view.GdxLevelFragment
import dev.lucasnlm.antimine.main.MainActivity
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.share.ShareManager
import dev.lucasnlm.antimine.splash.SplashActivity
import dev.lucasnlm.antimine.tutorial.view.TutorialCompleteDialogFragment
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameActivity :
    ThematicActivity(R.layout.activity_game),
    DialogInterface.OnDismissListener,
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

    private var status: Status = Status.PreGame
    private var currentTime: Long = 0
    private var currentSaveId: Long = 0

    private val areaSizeMultiplier by lazy { preferencesRepository.squareSizeMultiplier() }
    private val currentRadius by lazy { preferencesRepository.squareRadius() }
    private val useHelp by lazy { preferencesRepository.useHelp() }

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
                extras.containsKey(START_TUTORIAL) -> {
                    gameViewModel.startNewGame(Difficulty.Standard)
                    gameViewModel.eventObserver.postValue(Event.StartTutorial)
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
        Transformations
            .distinctUntilChanged(eventObserver)
            .observe(
                this@GameActivity,
                ::onGameEvent,
            )

        retryObserver.observe(
            this@GameActivity,
            {
                lifecycleScope.launch {
                    gameViewModel.retryGame(currentSaveId.toInt())
                }
            }
        )

        continueObserver.observe(
            this@GameActivity,
            {
                lifecycleScope.launch {
                    gameViewModel.onContinueFromGameOver()
                    eventObserver.postValue(Event.ResumeGame)
                }
            }
        )

        shareObserver.observe(
            this@GameActivity,
            {
                shareCurrentGame()
            }
        )

        elapsedTimeSeconds.observe(
            this@GameActivity,
            {
                timer.apply {
                    visibility = if (it == 0L) View.GONE else View.VISIBLE
                    text = DateUtils.formatElapsedTime(it)
                }
                currentTime = it
            }
        )

        mineCount.observe(
            this@GameActivity,
            {
                minesCount.apply {
                    if (it < 0) {
                        text.toString().toIntOrNull()?.let { oldValue ->
                            if (oldValue > it) {
                                startAnimation(AnimationUtils.loadAnimation(context, R.anim.fast_shake))
                            }
                        }
                    }

                    visibility = View.VISIBLE
                    text = it.toString()
                }
            }
        )

        saveId.observe(
            this@GameActivity,
            {
                currentSaveId = it
            }
        )

        tips.observe(
            this@GameActivity,
            {
                tipsCounter.text = it.toString()
            }
        )
    }

    override fun onResume() {
        super.onResume()
        val willReset = restartIfNeed()
        if (!willReset) {
            if (status == Status.Running) {
                gameViewModel.run {
                    refreshUserPreferences()
                    resumeGame()
                }

                analyticsManager.sentEvent(Analytics.Resume)
            }
        }

        keepScreenOn(true)
    }

    override fun onPause() {
        super.onPause()
        keepScreenOn(false)

        if (status == Status.Running) {
            gameViewModel.pauseGame()
        }

        cloudSaveManager.uploadSave()

        if (isFinishing) {
            analyticsManager.sentEvent(Analytics.Quit)
        }
    }

    private fun backToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        backToMainActivity()
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

    private fun disableShortcutIcon(hide: Boolean = false) {
        tipsCounter.visibility = View.GONE
        shortcutIcon.apply {
            visibility = if (hide) View.GONE else View.VISIBLE
            isClickable = false
            animate().alpha(0.3f).start()
        }
    }

    private fun refreshInGameShortcut() {
        if (preferencesRepository.useHelp()) {
            refreshTipShortcutIcon()
        } else {
            refreshRetryShortcut()
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

    private fun refreshRetryShortcut() {
        shortcutIcon.apply {
            TooltipCompat.setTooltipText(this, getString(R.string.new_game))
            setImageResource(R.drawable.retry)
            setColorFilter(minesCount.currentTextColor)
            setOnClickListener {
                lifecycleScope.launch {
                    val confirmResign = status == Status.Running
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
            when (status) {
                is Status.Over, is Status.Running -> {
                    isClickable = true
                    animate().alpha(1.0f).start()
                }
                else -> {
                    isClickable = false
                    animate().alpha(0.3f).start()
                }
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
            if (!preferencesRepository.isTutorialCompleted()) {
                loadGameTutorial()
            } else {
                loadGameFragment()
            }
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

    private fun loadGameTutorial() {
        disableShortcutIcon(false)

        supportFragmentManager.apply {
            findFragmentById(R.id.levelContainer)?.let { it ->
                beginTransaction().apply {
                    remove(it)
                    commitAllowingStateLoss()
                }
            }

            beginTransaction().apply {
                replace(R.id.levelContainer, TutorialLevelFragment(), TutorialLevelFragment.TAG)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                commitAllowingStateLoss()
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

    private fun showCompletedTutorialDialog() {
        TutorialCompleteDialogFragment().run {
            showAllowingStateLoss(supportFragmentManager, TutorialCompleteDialogFragment.TAG)
        }
    }

    private fun showEndGameDialog(gameResult: GameResult, canContinue: Boolean) {
        val currentGameStatus = status
        if (currentGameStatus is Status.Over && !isFinishing) {
            if (supportFragmentManager.findFragmentByTag(GameOverDialogFragment.TAG) == null &&
                supportFragmentManager.findFragmentByTag(WinGameDialogFragment.TAG) == null
            ) {
                val score = currentGameStatus.score

                if (gameResult == GameResult.Victory) {
                    WinGameDialogFragment.newInstance(
                        gameResult,
                        canContinue,
                        score?.rightMines ?: 0,
                        score?.totalMines ?: 0,
                        currentGameStatus.time,
                        2
                    ).apply {
                        showAllowingStateLoss(supportFragmentManager, WinGameDialogFragment.TAG)
                    }
                } else {
                    GameOverDialogFragment.newInstance(
                        gameResult,
                        canContinue,
                        score?.rightMines ?: 0,
                        score?.totalMines ?: 0,
                        currentGameStatus.time,
                        1
                    ).apply {
                        showAllowingStateLoss(supportFragmentManager, GameOverDialogFragment.TAG)
                    }
                }
            }
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

    private fun showEndGameAlert(gameResult: GameResult, canContinue: Boolean) {
        val canShowWindow = preferencesRepository.showWindowsWhenFinishGame()
        if (!isFinishing) {
            if (canShowWindow) {
                showEndGameDialog(gameResult, gameResult == GameResult.GameOver && canContinue)
            } else {
                if (gameResult == GameResult.GameOver) {
                    gameViewModel.viewModelScope.launch {
                        gameViewModel.revealMines()
                    }
                } else {
                    gameViewModel.viewModelScope.launch {
                        gameViewModel.flagAllMines()
                    }
                }

                showEndGameToast(gameResult)
            }
        }
    }

    private fun waitAndShowEndGameAlert(gameResult: GameResult, await: Boolean, canContinue: Boolean) {
        if (await && gameViewModel.explosionDelay() != 0L) {
            lifecycleScope.launch {
                delay((gameViewModel.explosionDelay() * 0.3).toLong())
                showEndGameAlert(gameResult, canContinue)
            }
        } else {
            showEndGameAlert(gameResult, canContinue)
        }
    }

    private fun onGameEvent(event: Event) {
        when (event) {
            Event.ResumeGame -> {
                status = Status.Running
                refreshInGameShortcut()
            }
            Event.StartNewGame -> {
                gameToast?.cancel()
                status = Status.PreGame
                disableShortcutIcon()
            }
            Event.Resume, Event.Running -> {
                status = Status.Running
                gameViewModel.runClock()
                refreshInGameShortcut()
                keepScreenOn(true)
            }
            Event.StartTutorial -> {
                status = Status.PreGame
                gameViewModel.stopClock()
                disableShortcutIcon(false)
                loadGameTutorial()
            }
            Event.FinishTutorial -> {
                preferencesRepository.setCompleteTutorial(true)
                gameViewModel.startNewGame(Difficulty.Beginner)
                disableShortcutIcon()
                status = Status.Over(0, Score(4, 4, 25))
                analyticsManager.sentEvent(Analytics.TutorialCompleted)
                showCompletedTutorialDialog()
            }
            Event.Victory -> {
                val isResuming = (status == Status.PreGame)
                val score = gameViewModel.getScore()
                status = Status.Over(currentTime, score)
                gameViewModel.stopClock()
                gameViewModel.showAllEmptyAreas()
                gameViewModel.victory()
                refreshRetryShortcut()
                keepScreenOn(false)

                if (!isResuming) {
                    lifecycleScope.launch {
                        gameViewModel.saveGame()
                        gameViewModel.saveStats()
                    }

                    if (gameViewModel.isCompletedWithMistakes()) {
                        gameViewModel.addNewTip(1)
                    } else {
                        gameViewModel.addNewTip(2)
                    }

                    waitAndShowEndGameAlert(
                        gameResult = GameResult.Victory,
                        await = false,
                        canContinue = false,
                    )
                }
            }
            Event.GameOver -> {
                val isResuming = (status == Status.PreGame)
                val score = gameViewModel.getScore()
                status = Status.Over(currentTime, score)
                refreshRetryShortcut()
                keepScreenOn(false)
                gameViewModel.stopClock()

                if (!isResuming) {
                    val isGameCompleted = gameViewModel.isCompletedWithMistakes()
                    lifecycleScope.launch {
                        gameViewModel.gameOver(isResuming, !isGameCompleted)
                        gameViewModel.saveGame()
                        waitAndShowEndGameAlert(
                            gameResult = if (isGameCompleted) GameResult.Completed else GameResult.GameOver,
                            await = true,
                            canContinue = gameViewModel.hasUnknownMines(),
                        )
                    }
                }
            }
            else -> {
            }
        }
    }

    /**
     * If user change any accessibility preference, the game will restart the activity to
     * apply these changes.
     */
    private fun restartIfNeed(): Boolean {
        return (
            areaSizeMultiplier != preferencesRepository.squareSizeMultiplier() ||
                currentRadius != preferencesRepository.squareRadius() ||
                useHelp != preferencesRepository.useHelp()
            ).also {
            if (it) {
                finish()
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }
    }

    private fun shareCurrentGame() {
        val levelSetup = gameViewModel.levelSetup.value
        val field = gameViewModel.field.value
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

    override fun onDismiss(dialog: DialogInterface?) {
        gameViewModel.run {
            refreshUserPreferences()
            resumeGame()
        }
    }

    override fun exit() {
        // LibGDX exit callback
    }

    companion object {
        val TAG = GameActivity::class.simpleName

        const val DIFFICULTY = "difficulty"
        const val START_TUTORIAL = "start_tutorial"
        const val START_GAME = "start_game"
        const val RETRY_GAME = "retry_game"
    }
}
