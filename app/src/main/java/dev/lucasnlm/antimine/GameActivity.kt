package dev.lucasnlm.antimine

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.doOnLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import dev.lucasnlm.antimine.about.AboutActivity
import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.models.Score
import dev.lucasnlm.antimine.common.level.models.Status
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.control.ControlDialogFragment
import dev.lucasnlm.antimine.core.analytics.IAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.models.Analytics
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.custom.CustomLevelDialogFragment
import dev.lucasnlm.antimine.history.HistoryActivity
import dev.lucasnlm.antimine.level.view.EndGameDialogFragment
import dev.lucasnlm.antimine.level.view.LevelFragment
import dev.lucasnlm.antimine.playgames.PlayGamesDialogFragment
import dev.lucasnlm.antimine.preferences.PreferencesActivity
import dev.lucasnlm.antimine.share.ShareManager
import dev.lucasnlm.antimine.stats.StatsActivity
import dev.lucasnlm.antimine.support.SupportAppDialogFragment
import dev.lucasnlm.antimine.theme.ThemeActivity
import dev.lucasnlm.antimine.tutorial.view.TutorialCompleteDialogFragment
import dev.lucasnlm.antimine.tutorial.view.TutorialLevelFragment
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.IInstantAppManager
import dev.lucasnlm.external.IPlayGamesManager
import dev.lucasnlm.external.ReviewWrapper
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_game.minesCount
import kotlinx.android.synthetic.main.activity_game.timer
import kotlinx.android.synthetic.main.activity_tv_game.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameActivity : ThematicActivity(R.layout.activity_game), DialogInterface.OnDismissListener {
    private val billingManager: IBillingManager by inject()

    private val preferencesRepository: IPreferencesRepository by inject()

    private val analyticsManager: IAnalyticsManager by inject()

    private val instantAppManager: IInstantAppManager by inject()

    private val savesRepository: ISavesRepository by inject()

    private val playGamesManager: IPlayGamesManager by inject()

    private val shareViewModel: ShareManager by inject()

    private val reviewWrapper: ReviewWrapper by inject()

    val gameViewModel by viewModel<GameViewModel>()

    override val noActionBar: Boolean = true

    private var status: Status = Status.PreGame
    private val areaSizeMultiplier by lazy { preferencesRepository.areaSizeMultiplier() }
    private var totalMines: Int = 0
    private var totalArea: Int = 0
    private var rightMines: Int = 0
    private var currentTime: Long = 0
    private var currentSaveId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        lifecycleScope.launchWhenCreated {
            bindViewModel()
        }

        bindToolbar()
        bindDrawer()
        bindNavigationMenu()
        bindAds()

        findViewById<FrameLayout>(R.id.levelContainer).doOnLayout {
            if (!isFinishing) {
                if (!preferencesRepository.isTutorialCompleted()) {
                    loadGameTutorial()
                } else {
                    loadGameFragment()
                }
            }
        }

        onOpenAppActions()
    }

    private fun bindViewModel() = gameViewModel.apply {
        var lastEvent: Event? = null // TODO use distinctUntilChanged when available

        eventObserver.observe(
            this@GameActivity,
            {
                if (lastEvent != it) {
                    onGameEvent(it)
                    lastEvent = it
                }
            }
        )

        retryObserver.observe(
            this@GameActivity,
            {
                lifecycleScope.launch {
                    gameViewModel.retryGame(currentSaveId.toInt())
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
                    visibility = View.VISIBLE
                    text = it.toString()
                }
            }
        )

        difficulty.observe(
            this@GameActivity,
            {
                onChangeDifficulty(it)
            }
        )

        field.observe(
            this@GameActivity,
            { area ->
                val mines = area.filter { it.hasMine }
                totalArea = area.count()
                totalMines = mines.count()
                rightMines = mines.count { it.mark.isFlag() }
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

    override fun onBackPressed() {
        when {
            drawer.isDrawerOpen(GravityCompat.START) -> {
                drawer.closeDrawer(GravityCompat.START)
                gameViewModel.resumeGame()
            }
            else -> super.onBackPressed()
        }
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

            silentGooglePlayLogin()
            refreshAds()
        }
    }

    override fun onPause() {
        super.onPause()

        if (status == Status.Running) {
            gameViewModel.pauseGame()
        }

        if (isFinishing) {
            analyticsManager.sentEvent(Analytics.Quit)
        }
    }

    private fun bindToolbar() {
        menu.apply {
            TooltipCompat.setTooltipText(this, getString(R.string.open_menu))
            setColorFilter(minesCount.currentTextColor)
            setOnClickListener {
                drawer.openDrawer(GravityCompat.START)
            }
        }

        minesCount.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(this, usingTheme.assets.toolbarMine),
            null,
            null,
            null
        )
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
                            GlobalScope.launch {
                                gameViewModel.startNewGame()
                            }
                        }
                    } else {
                        GlobalScope.launch {
                            gameViewModel.startNewGame()
                        }
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

    private fun bindDrawer() {
        drawer.apply {
            addDrawerListener(
                ActionBarDrawerToggle(
                    this@GameActivity,
                    drawer,
                    toolbar,
                    R.string.open_menu,
                    R.string.close_menu
                ).apply {
                    syncState()
                }
            )

            addDrawerListener(
                object : DrawerLayout.DrawerListener {
                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                        // Empty
                    }

                    override fun onDrawerOpened(drawerView: View) {
                        gameViewModel.pauseGame()
                        analyticsManager.sentEvent(Analytics.OpenDrawer)
                    }

                    override fun onDrawerClosed(drawerView: View) {
                        if (hasNoOtherFocusedDialog() && hasActiveGameFragment()) {
                            gameViewModel.resumeGame()
                        }

                        analyticsManager.sentEvent(Analytics.CloseDrawer)
                    }

                    override fun onDrawerStateChanged(newState: Int) {
                        // Empty
                    }
                }
            )

            if (preferencesRepository.isFirstUse() &&
                (preferencesRepository.isTutorialCompleted())
            ) {
                openDrawer(GravityCompat.START)
                preferencesRepository.completeFirstUse()
            }
        }
    }

    private fun bindNavigationMenu() {
        navigationView.setNavigationItemSelectedListener { item ->
            var handled = true

            when (item.itemId) {
                R.id.standard -> changeDifficulty(Difficulty.Standard)
                R.id.beginner -> changeDifficulty(Difficulty.Beginner)
                R.id.intermediate -> changeDifficulty(Difficulty.Intermediate)
                R.id.expert -> changeDifficulty(Difficulty.Expert)
                R.id.custom -> showCustomLevelDialog()
                R.id.control -> showControlDialog()
                R.id.about -> showAbout()
                R.id.settings -> showSettings()
                R.id.rate -> openRateUsLink()
                R.id.themes -> openThemes()
                R.id.share_now -> shareCurrentGame()
                R.id.previous_games -> openSaveHistory()
                R.id.stats -> openStats()
                R.id.play_games -> googlePlay()
                R.id.translation -> openCrowdIn()
                R.id.remove_ads -> showSupportAppDialog()
                R.id.tutorial -> loadGameTutorial()
                else -> handled = false
            }

            if (handled) {
                drawer.closeDrawer(GravityCompat.START)
            }

            handled
        }

        navigationView.menu.apply {
            findItem(R.id.share_now).isVisible = !instantAppManager.isEnabled(applicationContext)
            findItem(R.id.remove_ads).isVisible = !preferencesRepository.isPremiumEnabled()

            if (!playGamesManager.hasGooglePlayGames()) {
                removeGroup(R.id.play_games_group)
            }

            // New Features
            findItem(R.id.themes).setActionView(R.layout.new_icon)
            findItem(R.id.tutorial).setActionView(R.layout.new_icon)
        }
    }

    private fun onOpenAppActions() {
        if (instantAppManager.isEnabled(applicationContext)) {
            // Instant App does nothing.
            savesRepository.setLimit(1)
        } else {
            val current = preferencesRepository.getUseCount()
            val shouldRequestRating = preferencesRepository.isRequestRatingEnabled()
            val shouldRequestSupport = if (billingManager.isEnabled()) {
                !preferencesRepository.isPremiumEnabled()
            } else {
                preferencesRepository.showSupport()
            }

            if (current >= MIN_USAGES_TO_IAP && shouldRequestSupport) {
                analyticsManager.sentEvent(Analytics.UnlockIapDialog)
                showSupportAppDialog()
            } else if (current >= MIN_USAGES_TO_RATING && shouldRequestRating) {
                analyticsManager.sentEvent(Analytics.ShowRatingRequest(current))
                reviewWrapper.startInAppReview(this)
            }

            preferencesRepository.incrementUseCount()
        }
    }

    private fun onChangeDifficulty(difficulty: Difficulty) {
        navigationView.menu.apply {
            arrayOf(
                Difficulty.Standard to findItem(R.id.standard),
                Difficulty.Beginner to findItem(R.id.beginner),
                Difficulty.Intermediate to findItem(R.id.intermediate),
                Difficulty.Expert to findItem(R.id.expert),
                Difficulty.Custom to findItem(R.id.custom)
            ).map {
                it.second to (if (it.first == difficulty) R.drawable.checked else R.drawable.unchecked)
            }.forEach { (menuItem, icon) ->
                menuItem.setIcon(icon)
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

            if (findFragmentByTag(LevelFragment.TAG) == null) {
                beginTransaction().apply {
                    replace(R.id.levelContainer, LevelFragment(), LevelFragment.TAG)
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    commitAllowingStateLoss()
                }
            }
        }
    }

    private fun loadGameTutorial() {
        disableShortcutIcon(true)

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
        AlertDialog.Builder(this).apply {
            setTitle(R.string.new_game)
            setMessage(R.string.retry_sure)
            setPositiveButton(R.string.resume) { _, _ -> action() }
            setNegativeButton(R.string.cancel, null)
            show()
        }
    }

    private fun showCustomLevelDialog() {
        if (supportFragmentManager.findFragmentByTag(CustomLevelDialogFragment.TAG) == null) {
            CustomLevelDialogFragment().apply {
                show(supportFragmentManager, CustomLevelDialogFragment.TAG)
            }
        }
    }

    private fun showControlDialog() {
        gameViewModel.pauseGame()

        if (supportFragmentManager.findFragmentByTag(CustomLevelDialogFragment.TAG) == null) {
            ControlDialogFragment().apply {
                show(supportFragmentManager, ControlDialogFragment.TAG)
            }
        }
    }

    private fun showAbout() {
        analyticsManager.sentEvent(Analytics.OpenAbout)
        Intent(this, AboutActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun openThemes() {
        analyticsManager.sentEvent(Analytics.OpenThemes)
        Intent(this, ThemeActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun openSaveHistory() {
        analyticsManager.sentEvent(Analytics.OpenSaveHistory)
        Intent(this, HistoryActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun openStats() {
        analyticsManager.sentEvent(Analytics.OpenStats)
        Intent(this, StatsActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun showSettings() {
        analyticsManager.sentEvent(Analytics.OpenSettings)
        Intent(this, PreferencesActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun showCompletedTutorialDialog() {
        TutorialCompleteDialogFragment().run {
            showAllowingStateLoss(supportFragmentManager, TutorialCompleteDialogFragment.TAG)
        }
    }

    private fun showEndGameDialog(victory: Boolean) {
        val currentGameStatus = status
        if (currentGameStatus is Status.Over && !isFinishing && !drawer.isDrawerOpen(GravityCompat.START)) {
            if (supportFragmentManager.findFragmentByTag(SupportAppDialogFragment.TAG) == null &&
                supportFragmentManager.findFragmentByTag(EndGameDialogFragment.TAG) == null
            ) {
                val score = currentGameStatus.score
                EndGameDialogFragment.newInstance(
                    victory,
                    score?.rightMines ?: 0,
                    score?.totalMines ?: 0,
                    currentGameStatus.time,
                    if (victory) 1 else 0
                ).apply {
                    showAllowingStateLoss(supportFragmentManager, EndGameDialogFragment.TAG)
                }
            }
        }
    }

    private fun waitAndShowEndGameDialog(victory: Boolean, await: Boolean) {
        if (await && gameViewModel.explosionDelay() != 0L) {
            lifecycleScope.launch {
                delay((gameViewModel.explosionDelay() * 0.3).toLong())
                showEndGameDialog(victory)
            }
        } else {
            showEndGameDialog(victory)
        }
    }

    private fun changeDifficulty(newDifficulty: Difficulty) {
        preferencesRepository.completeTutorial()

        if (status == Status.PreGame) {
            loadGameFragment()
            lifecycleScope.launch {
                gameViewModel.startNewGame(newDifficulty)
            }
        } else {
            newGameConfirmation {
                loadGameFragment()
                lifecycleScope.launch {
                    gameViewModel.startNewGame(newDifficulty)
                }
            }
        }
    }

    private fun onGameEvent(event: Event) {
        when (event) {
            Event.ResumeGame -> {
                status = Status.Running
                refreshInGameShortcut()
            }
            Event.StartNewGame -> {
                status = Status.PreGame
                disableShortcutIcon()
                refreshAds()
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
                disableShortcutIcon(true)
                loadGameTutorial()
            }
            Event.FinishTutorial -> {
                gameViewModel.startNewGame(Difficulty.Beginner)
                disableShortcutIcon()
                loadGameFragment()
                status = Status.Over(0, Score(4, 4, 25))
                analyticsManager.sentEvent(Analytics.TutorialCompleted)
                preferencesRepository.completeTutorial()
                showCompletedTutorialDialog()
            }
            Event.Victory -> {
                val isResuming = (status == Status.PreGame)
                val score = Score(
                    rightMines,
                    totalMines,
                    totalArea
                )
                status = Status.Over(currentTime, score)
                gameViewModel.stopClock()
                gameViewModel.showAllEmptyAreas()
                gameViewModel.victory()
                refreshRetryShortcut()
                keepScreenOn(false)

                if (!isResuming) {
                    gameViewModel.addNewTip()

                    waitAndShowEndGameDialog(
                        victory = true,
                        await = false
                    )
                }
            }
            Event.GameOver -> {
                val isResuming = (status == Status.PreGame)
                val score = Score(
                    rightMines,
                    totalMines,
                    totalArea
                )
                status = Status.Over(currentTime, score)
                refreshRetryShortcut()
                keepScreenOn(false)
                gameViewModel.stopClock()

                if (!isResuming) {
                    GlobalScope.launch(context = Dispatchers.Main) {
                        gameViewModel.gameOver(isResuming)
                        waitAndShowEndGameDialog(
                            victory = false,
                            await = true
                        )
                    }
                }
            }
            else -> { }
        }
    }

    /**
     * If user change any accessibility preference, the game will restart the activity to
     * apply these changes.
     */
    private fun restartIfNeed(): Boolean {
        return (areaSizeMultiplier != preferencesRepository.areaSizeMultiplier()).also {
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

    private fun openRateUsLink() {
        reviewWrapper.startReviewPage(this, BuildConfig.VERSION_NAME)
        analyticsManager.sentEvent(Analytics.TapRatingRequest)
        preferencesRepository.disableRequestRating()
    }

    private fun keepScreenOn(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun hasNoOtherFocusedDialog(): Boolean {
        return supportFragmentManager.fragments.count {
            it !is LevelFragment && it is DialogFragment
        } == 0
    }

    private fun hasActiveGameFragment(): Boolean {
        return supportFragmentManager.findFragmentByTag(LevelFragment.TAG) != null
    }

    override fun onDismiss(dialog: DialogInterface?) {
        gameViewModel.run {
            refreshUserPreferences()
            resumeGame()
        }

        refreshAds()
    }

    private fun bindAds() {
        refreshAds()

        if (!preferencesRepository.isPremiumEnabled()) {
            lifecycleScope.launchWhenCreated {
                billingManager.listenPurchases().collect {
                    if (it is PurchaseInfo.PurchaseResult) {
                        if (it.unlockStatus && !isFinishing) {
                            refreshAds()
                        }
                    }
                }
            }
        }
    }

    private fun refreshAds() {
        if (!preferencesRepository.isPremiumEnabled() && billingManager.isEnabled()) {
            if (!instantAppManager.isEnabled(this)) {
                navigationView.menu.setGroupVisible(R.id.remove_ads_group, true)
                ad_placeholder.visibility = View.VISIBLE
                ad_placeholder.loadAd()
            }
        } else {
            navigationView.menu.setGroupVisible(R.id.remove_ads_group, false)
            ad_placeholder.visibility = View.GONE
        }
    }

    private fun silentGooglePlayLogin() {
        if (playGamesManager.hasGooglePlayGames()) {
            playGamesManager.silentLogin(this)
            invalidateOptionsMenu()
        }
    }

    private fun googlePlay() {
        if (playGamesManager.isLogged()) {
            if (supportFragmentManager.findFragmentByTag(PlayGamesDialogFragment.TAG) == null) {
                PlayGamesDialogFragment().show(supportFragmentManager, PlayGamesDialogFragment.TAG)
            }
        } else {
            playGamesManager.getLoginIntent()?.let {
                startActivityForResult(it, GOOGLE_PLAY_REQUEST_CODE)
            }
        }
    }

    private fun openCrowdIn() {
        Intent(Intent.ACTION_VIEW, Uri.parse("https://crowdin.com/project/antimine-android")).let {
            startActivity(it)
        }
    }

    private fun showSupportAppDialog() {
        if (supportFragmentManager.findFragmentByTag(SupportAppDialogFragment.TAG) == null &&
            !instantAppManager.isEnabled(this)
        ) {
            if (billingManager.isEnabled()) {
                SupportAppDialogFragment.newRemoveAdsSupportDialog()
                    .show(supportFragmentManager, SupportAppDialogFragment.TAG)
            } else {
                SupportAppDialogFragment.newRequestSupportDialog()
                    .show(supportFragmentManager, SupportAppDialogFragment.TAG)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_PLAY_REQUEST_CODE) {
            playGamesManager.handleLoginResult(data)
            invalidateOptionsMenu()
        }
    }

    companion object {
        const val GOOGLE_PLAY_REQUEST_CODE = 6

        const val MIN_USAGES_TO_IAP = 5
        const val MIN_USAGES_TO_RATING = 10
    }
}
