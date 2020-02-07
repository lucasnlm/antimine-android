package dev.lucasnlm.antimine.wear

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.wear.widget.SwipeDismissFrameLayout
import dagger.android.support.DaggerAppCompatActivity
import dev.lucasnlm.antimine.common.level.data.GameStatus
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import kotlinx.android.synthetic.main.activity_level.*
import javax.inject.Inject
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.core.os.HandlerCompat
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.ambient.AmbientModeSupport.AmbientCallback
import androidx.wear.ambient.AmbientModeSupport.EXTRA_LOWBIT_AMBIENT
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.data.AmbientSettings
import dev.lucasnlm.antimine.common.level.data.GameEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WatchGameActivity : DaggerAppCompatActivity(), AmbientModeSupport.AmbientCallbackProvider {

    @Inject
    lateinit var viewModelFactory: GameViewModelFactory

    @Inject
    lateinit var preferencesRepository: IPreferencesRepository

    private lateinit var viewModel: GameViewModel
    private lateinit var ambientController: AmbientModeSupport.AmbientController

    private var currentLevelFragment: WatchLevelFragment? = null

    private val clock = Clock()
    private var lastShownTime: String? = null
    private var gameStatus: GameStatus = GameStatus.PreGame

    private var ambientMode: AmbientCallback = object : AmbientCallback() {
        override fun onExitAmbient() {
            super.onExitAmbient()
            currentLevelFragment?.setAmbientMode(
                AmbientSettings(
                    isAmbientMode = false,
                    isLowBitAmbient = false
                )
            )
        }

        override fun onEnterAmbient(ambientDetails: Bundle?) {
            super.onEnterAmbient(ambientDetails)
            val lowBit = ambientDetails?.getBoolean(EXTRA_LOWBIT_AMBIENT) ?: true
            currentLevelFragment?.setAmbientMode(AmbientSettings(true, lowBit))
            updateClockText(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(GameViewModel::class.java)

        ambientController = AmbientModeSupport.attach(this)

        setContentView(R.layout.activity_level)

        bindViewModel()

        loadGameFragment()

        swipe.addCallback(object : SwipeDismissFrameLayout.Callback() {
            override fun onDismissed(layout: SwipeDismissFrameLayout) {
                swipe.visibility = View.GONE
                finish()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        clock.start {
            GlobalScope.launch(Dispatchers.Main) {
                updateClockText()
            }
        }
    }

    private fun updateClockText(force: Boolean = false) {
        val dateFormat = DateFormat.getTimeFormat(applicationContext)
        val current = dateFormat.format(System.currentTimeMillis())
        if (force || lastShownTime != current) {
            messageText.text = current
            lastShownTime = current
        }
    }

    override fun onPause() {
        super.onPause()
        clock.stop()
    }

    private fun loadGameFragment() {
        val fragmentManager = supportFragmentManager

        fragmentManager.popBackStack()

        fragmentManager.findFragmentById(R.id.level_container)?.let { it ->
            fragmentManager.beginTransaction().apply {
                remove(it)
                commitAllowingStateLoss()
            }
        }

        val levelFragment = WatchLevelFragment()

        fragmentManager.beginTransaction().apply {
            replace(R.id.level_container, levelFragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            commitAllowingStateLoss()
        }

        currentLevelFragment = levelFragment
    }

    private fun bindViewModel() = viewModel.apply {
        eventObserver.observe(this@WatchGameActivity, Observer {
            onGameEvent(it)
        })
        elapsedTimeSeconds.observe(this@WatchGameActivity, Observer {
            // Nothing
        })
        mineCount.observe(this@WatchGameActivity, Observer {
            if (it > 0) {
                messageText.text = applicationContext.getString(R.string.mines_remaining, it)
            }
        })
        difficulty.observe(this@WatchGameActivity, Observer {
            // Nothing
        })
    }

    private fun onGameEvent(event: GameEvent) {
        when (event) {
            GameEvent.ResumeGame -> {
            }
            GameEvent.StartNewGame -> {
                gameStatus = GameStatus.PreGame
            }
            GameEvent.Resume, GameEvent.Running -> {
                gameStatus = GameStatus.Running
            }
            GameEvent.Victory -> {
                gameStatus = GameStatus.Over

                messageText.text = getString(R.string.victory)
                waitAndShowNewGameButton()
            }
            GameEvent.GameOver -> {
                gameStatus = GameStatus.Over
                viewModel.stopClock()
                viewModel.gameOver()

                messageText.text = getString(R.string.game_over)
                waitAndShowNewGameButton()
            }
            GameEvent.ResumeVictory -> {
                gameStatus = GameStatus.Over
                messageText.text = getString(R.string.victory)
            }
            GameEvent.ResumeGameOver -> {
                gameStatus = GameStatus.Over
                messageText.text = getString(R.string.game_over)
            }
            else -> {
            }
        }
    }

    private fun waitAndShowNewGameButton() {
        HandlerCompat.postDelayed(Handler(), {
            if (this.gameStatus == GameStatus.Over && !isFinishing) {
                newGame.visibility = View.VISIBLE
                newGame.setOnClickListener {
                    it.visibility = View.GONE
                    GlobalScope.launch {
                        viewModel.startNewGame()
                    }
                }
            }
        }, null, DateUtils.SECOND_IN_MILLIS)
    }

    override fun getAmbientCallback(): AmbientCallback = ambientMode
}
