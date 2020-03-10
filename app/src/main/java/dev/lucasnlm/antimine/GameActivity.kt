package dev.lucasnlm.antimine

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat.postDelayed
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.android.support.DaggerAppCompatActivity
import dev.lucasnlm.antimine.about.AboutActivity
import dev.lucasnlm.antimine.common.level.data.DifficultyPreset
import dev.lucasnlm.antimine.common.level.data.GameEvent
import dev.lucasnlm.antimine.common.level.data.GameStatus
import dev.lucasnlm.antimine.common.level.data.Mark
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import dev.lucasnlm.antimine.core.analytics.AnalyticsManager
import dev.lucasnlm.antimine.core.analytics.Event
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.utils.isDarkModeEnabled
import dev.lucasnlm.antimine.instant.InstantAppManager
import dev.lucasnlm.antimine.level.view.CustomLevelDialogFragment
import dev.lucasnlm.antimine.level.view.EndGameDialogFragment
import dev.lucasnlm.antimine.level.view.LevelFragment
import dev.lucasnlm.antimine.preferences.PreferencesActivity
import dev.lucasnlm.antimine.share.viewmodel.ShareViewModel
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class GameActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: GameViewModelFactory

    @Inject
    lateinit var preferencesRepository: IPreferencesRepository

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    @Inject
    lateinit var instantAppManager: InstantAppManager

    private lateinit var viewModel: GameViewModel
    private lateinit var shareViewModel: ShareViewModel

    private var gameStatus: GameStatus = GameStatus.PreGame
    private val usingLargeArea by lazy { preferencesRepository.useLargeAreas() }
    private var totalMines: Int = 0
    private var rightMines: Int = 0
    private var currentTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GameViewModel::class.java)
        shareViewModel = ViewModelProviders.of(this).get(ShareViewModel::class.java)

        bindViewModel()
        bindToolbarAndDrawer()
        loadGameFragment()

        if (instantAppManager.isEnabled()) {
            bindInstantApp()
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                checkUpdate()
            }

            checkUseCount()
        }
    }

    private fun bindViewModel() = viewModel.apply {
        eventObserver.observe(this@GameActivity, Observer {
            onGameEvent(it)
        })

        elapsedTimeSeconds.observe(this@GameActivity, Observer {
            timer.apply {
                visibility = if (it == 0L) View.GONE else View.VISIBLE
                text = DateUtils.formatElapsedTime(it)
            }
            currentTime = it
        })

        mineCount.observe(this@GameActivity, Observer {
            minesCount.apply {
                visibility = View.VISIBLE
                text = it.toString()
            }
        })

        difficulty.observe(this@GameActivity, Observer {
            onChangeDifficulty(it)
        })

        field.observe(this@GameActivity, Observer { area ->
            val mines = area.filter { it.hasMine }
            totalMines = mines.count()
            rightMines = mines.map { if (it.mark == Mark.Flag) 1 else 0 }.sum()
        })
    }

    override fun onBackPressed() {
        when {
            drawer.isDrawerOpen(GravityCompat.START) -> {
                drawer.closeDrawer(GravityCompat.START)
                viewModel.resumeGame()
            }
            gameStatus == GameStatus.Running -> showQuitConfirmation {
                super.onBackPressed()
            }
            else -> super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        if (gameStatus == GameStatus.Running) {
            viewModel.resumeGame()
            analyticsManager.sentEvent(Event.Resume())
        }

        restartIfNeed()
    }

    override fun onPause() {
        super.onPause()

        if (gameStatus == GameStatus.Running) {
            viewModel.pauseGame()
        }

        analyticsManager.sentEvent(Event.Quit())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean =
        when (gameStatus) {
            is GameStatus.Over, is GameStatus.Running -> {
                menuInflater.inflate(R.menu.top_menu_over, menu)
                true
            }
            else -> true
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.reset) {

            val confirmResign = gameStatus == GameStatus.Running
            analyticsManager.sentEvent(Event.TapGameReset(confirmResign))

            if (confirmResign) {
                newGameConfirmation {
                    GlobalScope.launch {
                        viewModel.startNewGame()
                    }
                }
            } else {
                GlobalScope.launch {
                    viewModel.startNewGame()
                }
            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun bindToolbarAndDrawer() {
        setSupportActionBar(toolbar)
        toolbar.title = ""

        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        drawer.apply {
            addDrawerListener(
                ActionBarDrawerToggle(
                    this@GameActivity,
                    drawer,
                    toolbar,
                    R.string.open_menu,
                    R.string.close_menu
                ).apply {
                    if (!isDarkModeEnabled(applicationContext)) {
                        drawerArrowDrawable.color =
                            ContextCompat.getColor(applicationContext, R.color.primary)
                    }

                    syncState()
                }
            )

            addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    // Empty
                }

                override fun onDrawerOpened(drawerView: View) {
                    if (gameStatus is GameStatus.Over) {
                        viewModel.pauseGame()
                    }
                    analyticsManager.sentEvent(Event.OpenDrawer())
                }

                override fun onDrawerClosed(drawerView: View) {
                    if (gameStatus is GameStatus.Over) {
                        viewModel.resumeGame()
                    }
                    analyticsManager.sentEvent(Event.CloseDrawer())
                }

                override fun onDrawerStateChanged(newState: Int) {
                    // Empty
                }
            })
        }

        navigationView.setNavigationItemSelectedListener { item ->
            var handled = true

            when (item.itemId) {
                R.id.standard -> changeDifficulty(DifficultyPreset.Standard)
                R.id.beginner -> changeDifficulty(DifficultyPreset.Beginner)
                R.id.intermediate -> changeDifficulty(DifficultyPreset.Intermediate)
                R.id.expert -> changeDifficulty(DifficultyPreset.Expert)
                R.id.custom -> showCustomLevelDialog()
                R.id.about -> showAbout()
                R.id.settings -> showSettings()
                R.id.rate -> openRateUsLink("Drawer")
                R.id.share_now -> shareCurrentGame()
                R.id.install_new -> installFromInstantApp()
                else -> handled = false
            }

            if (handled) {
                drawer.closeDrawer(GravityCompat.START)
            }

            handled
        }

        navigationView.menu.findItem(R.id.share_now).isVisible = instantAppManager.isNotEnabled()

        if (preferencesRepository.getBoolean(PREFERENCE_FIRST_USE, false)) {
            drawer.openDrawer(GravityCompat.START)
            preferencesRepository.putBoolean(PREFERENCE_FIRST_USE, true)
        }
    }

    private fun checkUseCount() {
        val current = preferencesRepository.getInt(PREFERENCE_USE_COUNT, 0)
        val shouldRequestRating = preferencesRepository.getBoolean(PREFERENCE_REQUEST_RATING, true)

        if (current >= 4 && shouldRequestRating) {
            analyticsManager.sentEvent(Event.ShowRatingRequest(current))
            showRequestRating()
        }

        preferencesRepository.putInt(PREFERENCE_USE_COUNT, current + 1)
    }

    private fun onChangeDifficulty(difficulty: DifficultyPreset) {
        navigationView.menu.apply {
            arrayOf(
                DifficultyPreset.Standard to findItem(R.id.standard),
                DifficultyPreset.Beginner to findItem(R.id.beginner),
                DifficultyPreset.Intermediate to findItem(R.id.intermediate),
                DifficultyPreset.Expert to findItem(R.id.expert),
                DifficultyPreset.Custom to findItem(R.id.custom)
            ).map {
                it.second to (if (it.first == difficulty) R.drawable.checked else R.drawable.unchecked)
            }.forEach { (menuItem, icon) ->
                menuItem.setIcon(icon)
            }
        }
    }

    private fun loadGameFragment() {
        val fragmentManager = supportFragmentManager

        fragmentManager.popBackStack()

        fragmentManager.findFragmentById(R.id.levelContainer)?.let { it ->
            fragmentManager.beginTransaction().apply {
                remove(it)
                commitAllowingStateLoss()
            }
        }

        fragmentManager.beginTransaction().apply {
            replace(R.id.levelContainer, LevelFragment())
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            commitAllowingStateLoss()
        }
    }

    private fun showRequestRating() {
        if (getString(R.string.rating_message).isNotEmpty()) {

            AlertDialog.Builder(this)
                .setMessage(R.string.rating_message)
                .setPositiveButton(R.string.rating_button) { _, _ ->
                    openRateUsLink("Dialog")
                }
                .setNegativeButton(R.string.rating_button_no) { _, _ ->
                    preferencesRepository.putBoolean(PREFERENCE_REQUEST_RATING, false)
                }
                .show()
        }
    }

    private fun newGameConfirmation(action: () -> Unit) {
        AlertDialog.Builder(this, R.style.MyDialog).apply {
            setTitle(R.string.start_over)
            setMessage(R.string.retry_sure)
            setPositiveButton(R.string.resume) { _, _ -> action() }
            setNegativeButton(R.string.cancel, null)
            show()
        }
    }

    private fun showQuitConfirmation(action: () -> Unit) {
        AlertDialog.Builder(this, R.style.MyDialog)
            .setTitle(R.string.are_you_sure)
            .setMessage(R.string.sure_quit_desc)
            .setPositiveButton(R.string.quit) { _, _ -> action() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showCustomLevelDialog() {
        if (supportFragmentManager.findFragmentByTag(CustomLevelDialogFragment.TAG) == null) {
            CustomLevelDialogFragment().apply {
                show(supportFragmentManager, CustomLevelDialogFragment.TAG)
            }
        }
    }

    private fun showAbout() {
        analyticsManager.sentEvent(Event.OpenAbout())
        Intent(this, AboutActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun showSettings() {
        analyticsManager.sentEvent(Event.OpenSettings())
        Intent(this, PreferencesActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun waitAndShowEndGameDialog(victory: Boolean, await: Long = DateUtils.SECOND_IN_MILLIS) {
        if (supportFragmentManager.findFragmentByTag(EndGameDialogFragment.TAG) == null) {
            postDelayed(Handler(), {
                if (gameStatus is GameStatus.Over && !isFinishing) {
                    val over = gameStatus as GameStatus.Over
                    EndGameDialogFragment.newInstance(victory, over.rightMines, over.totalMines, over.time).apply {
                        show(supportFragmentManager, EndGameDialogFragment.TAG)
                    }
                }
            }, null, await)
        }
    }

    private fun changeDifficulty(newDifficulty: DifficultyPreset) {
        if (gameStatus == GameStatus.PreGame) {
            GlobalScope.launch {
                viewModel.startNewGame(newDifficulty)
            }
        } else {
            newGameConfirmation {
                GlobalScope.launch {
                    viewModel.startNewGame(newDifficulty)
                }
            }
        }
    }

    private fun onGameEvent(event: GameEvent) {
        when (event) {
            GameEvent.ResumeGame -> {
                invalidateOptionsMenu()
            }
            GameEvent.StartNewGame -> {
                gameStatus = GameStatus.PreGame
                invalidateOptionsMenu()
            }
            GameEvent.Resume, GameEvent.Running -> {
                gameStatus = GameStatus.Running
                viewModel.runClock()
                invalidateOptionsMenu()
            }
            GameEvent.Victory -> {
                gameStatus = GameStatus.Over(rightMines, totalMines, currentTime)
                viewModel.stopClock()
                viewModel.revealAllEmptyAreas()
                viewModel.victory()
                invalidateOptionsMenu()
                waitAndShowEndGameDialog(true, 0L)
            }
            GameEvent.GameOver -> {
                gameStatus = GameStatus.Over(rightMines, totalMines, currentTime)
                invalidateOptionsMenu()
                viewModel.stopClock()
                viewModel.gameOver()

                waitAndShowEndGameDialog(false)
            }
            GameEvent.ResumeVictory -> {
                gameStatus = GameStatus.Over(rightMines, totalMines, currentTime)
                invalidateOptionsMenu()
                viewModel.stopClock()

                waitAndShowEndGameDialog(true)
            }
            GameEvent.ResumeGameOver -> {
                gameStatus = GameStatus.Over(rightMines, totalMines, currentTime)
                invalidateOptionsMenu()
                viewModel.stopClock()

                waitAndShowEndGameDialog(false)
            }
            else -> { }
        }
    }

    /**
     * Call Google API to request update.
     */
    private fun checkUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        info, AppUpdateType.FLEXIBLE, this, 1
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Fail to request update.")
                }
            }
        }
    }

    /**
     * If user change any accessibility preference, the game will restart the activity to
     * apply these changes.
     */
    private fun restartIfNeed() {
        if (usingLargeArea != preferencesRepository.useLargeAreas()) {
            finish()
            Intent(this, GameActivity::class.java).run { startActivity(this) }
        }
    }

    private fun shareCurrentGame() {
        val levelSetup = viewModel.levelSetup.value
        val field = viewModel.field.value
        val spentTime: Long? = if (gameStatus is GameStatus.Over) currentTime else null
        GlobalScope.launch {
            shareViewModel.share(levelSetup, field, spentTime)
        }
    }

    private fun bindInstantApp() {
        findViewById<View>(R.id.install).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                installFromInstantApp()
            }
        }

        navigationView.menu.setGroupVisible(R.id.install_group, true)
    }

    private fun installFromInstantApp() {
        instantAppManager.showInstallPrompt(this@GameActivity, null, IA_REQUEST_CODE, IA_REFERRER)
    }

    private fun openRateUsLink(from: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }

        analyticsManager.sentEvent(Event.TapRatingRequest(from))
        preferencesRepository.putBoolean(PREFERENCE_REQUEST_RATING, false)
    }

    companion object {
        val TAG = GameActivity::class.simpleName
        const val PREFERENCE_FIRST_USE = "preference_first_use"
        const val PREFERENCE_USE_COUNT = "preference_use_count"
        const val PREFERENCE_REQUEST_RATING = "preference_request_rating"

        const val IA_REFERRER = "InstallApiActivity"
        const val IA_REQUEST_CODE = 5
    }
}
