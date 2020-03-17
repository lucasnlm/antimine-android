package dev.lucasnlm.antimine

import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.os.HandlerCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.android.support.DaggerAppCompatActivity
import dev.lucasnlm.antimine.about.AboutActivity
import dev.lucasnlm.antimine.common.level.models.*
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.level.view.CustomLevelDialogFragment
import dev.lucasnlm.antimine.level.view.LevelFragment
import dev.lucasnlm.antimine.preferences.PreferencesActivity
import kotlinx.android.synthetic.main.activity_tv_game.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class TvGameActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: GameViewModelFactory

    @Inject
    lateinit var preferencesRepository: IPreferencesRepository

    private lateinit var viewModel: GameViewModel

    private var status: Status = Status.PreGame
    private var totalMines: Int = 0
    private var totalArea: Int = 0
    private var rightMines: Int = 0
    private var currentTime: Long = 0

    private var keepConfirmingNewGame = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_game)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GameViewModel::class.java)
        bindViewModel()

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        loadGameFragment()

        if (Build.VERSION.SDK_INT >= 21) {
            checkUpdate()
        }
    }

    private fun bindViewModel() = viewModel.apply {
        eventObserver.observe(this@TvGameActivity, Observer {
            onGameEvent(it)
        })

        elapsedTimeSeconds.observe(this@TvGameActivity, Observer {
            timer.apply {
                visibility = if (it == 0L) View.GONE else View.VISIBLE
                text = DateUtils.formatElapsedTime(it)
            }
            currentTime = it
        })

        mineCount.observe(this@TvGameActivity, Observer {
            minesCount.apply {
                visibility = View.VISIBLE
                text = it.toString()
            }
        })

        difficulty.observe(this@TvGameActivity, Observer {
            // onChangeDifficulty(it)
        })

        field.observe(this@TvGameActivity, Observer { area ->
            val mines = area.filter { it.hasMine }
            totalArea = area.count()
            totalMines = mines.count()
            rightMines = mines.map { if (it.mark == Mark.Flag) 1 else 0 }.sum()
        })
    }

    override fun onResume() {
        super.onResume()
        if (status == Status.Running) {
            viewModel.resumeGame()
        }
    }

    override fun onPause() {
        super.onPause()

        if (status == Status.Running) {
            viewModel.pauseGame()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean =
        when (status) {
            is Status.Over, is Status.Running -> {
                menuInflater.inflate(R.menu.top_menu_over, menu)
                true
            }
            else -> true
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.reset) {
            if (status == Status.Running) {
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
            .setMessage(R.string.quit_confirm)
            .setPositiveButton(R.string.quit) { _, _ -> action() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showCustomLevelDialog() {
        CustomLevelDialogFragment().apply {
            show(supportFragmentManager, "custom_level_fragment")
        }
    }

    private fun showAbout() {
        Intent(this, AboutActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun showSettings() {
        Intent(this, PreferencesActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun showVictory() {
        AlertDialog.Builder(this, R.style.MyDialog).apply {
            setTitle(R.string.you_won)
            setMessage(R.string.all_mines_disabled)
            setCancelable(false)
            setPositiveButton(R.string.new_game) { _, _ ->
                GlobalScope.launch {
                    viewModel.startNewGame()
                }
            }
            setNegativeButton(R.string.cancel, null)
            show()
        }
    }

    private fun waitAndShowConfirmNewGame() {
        if (keepConfirmingNewGame) {
            HandlerCompat.postDelayed(Handler(), {
                if (status is Status.Over && !isFinishing) {
                    AlertDialog.Builder(this, R.style.MyDialog).apply {
                        setTitle(R.string.new_game)
                        setMessage(R.string.new_game_request)
                        setPositiveButton(R.string.yes) { _, _ ->
                            GlobalScope.launch {
                                viewModel.startNewGame()
                            }
                        }
                        setNegativeButton(R.string.cancel, null)
                    }.show()

                    keepConfirmingNewGame = false
                }
            }, null, DateUtils.SECOND_IN_MILLIS)
        }
    }

    private fun waitAndShowGameOverConfirmNewGame() {
        HandlerCompat.postDelayed(Handler(), {
            if (status is Status.Over && !isFinishing) {
                AlertDialog.Builder(this, R.style.MyDialog).apply {
                    setTitle(R.string.you_lost)
                    setMessage(R.string.new_game_request)
                    setPositiveButton(R.string.yes) { _, _ ->
                        GlobalScope.launch {
                            viewModel.startNewGame()
                        }
                    }
                    setNegativeButton(R.string.cancel, null)
                }.show()
            }
        }, null, DateUtils.SECOND_IN_MILLIS)
    }

    private fun changeDifficulty(newDifficulty: DifficultyPreset) {
        if (status == Status.PreGame) {
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

    private fun onGameEvent(event: Event) {
        when (event) {
            Event.ResumeGame -> {
                invalidateOptionsMenu()
            }
            Event.StartNewGame -> {
                status = Status.PreGame
                invalidateOptionsMenu()
            }
            Event.Resume, Event.Running -> {
                status = Status.Running
                viewModel.runClock()
                invalidateOptionsMenu()
            }
            Event.Victory -> {
                val score = Score(
                    rightMines,
                    totalMines,
                    totalArea
                )
                status = Status.Over(currentTime, score)
                viewModel.stopClock()
                viewModel.revealAllEmptyAreas()
                invalidateOptionsMenu()
                showVictory()
            }
            Event.GameOver -> {
                val score = Score(
                    rightMines,
                    totalMines,
                    totalArea
                )
                status = Status.Over(currentTime, score)
                invalidateOptionsMenu()
                viewModel.stopClock()
                viewModel.gameOver()

                waitAndShowGameOverConfirmNewGame()
            }
            Event.ResumeVictory, Event.ResumeGameOver -> {
                val score = Score(
                    rightMines,
                    totalMines,
                    totalArea
                )
                status = Status.Over(currentTime, score)
                invalidateOptionsMenu()
                viewModel.stopClock()

                waitAndShowConfirmNewGame()
            }
            else -> { }
        }
    }

    private fun checkUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        info, AppUpdateType.FLEXIBLE, this, 1)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Fail to request update.")
                }
            }
        }
    }

    companion object {
        const val TAG = "GameActivity"
    }
}
