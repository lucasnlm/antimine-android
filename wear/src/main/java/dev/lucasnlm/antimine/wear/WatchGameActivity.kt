package dev.lucasnlm.antimine.wear

import android.os.Bundle
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.ambient.AmbientModeSupport.AmbientCallback
import androidx.wear.ambient.AmbientModeSupport.EXTRA_LOWBIT_AMBIENT
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.models.AmbientSettings
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.models.Status
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import kotlinx.android.synthetic.main.activity_level.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class WatchGameActivity : AppCompatActivity(R.layout.activity_level), AmbientModeSupport.AmbientCallbackProvider {
    private val viewModel by viewModel<GameViewModel>()
    private val preferencesRepository: IPreferencesRepository by inject()

    private var currentLevelFragment: WatchLevelFragment? = null
    private val clock = Clock()
    private var lastShownTime: String? = null
    private var status: Status = Status.PreGame

    private var ambientMode: AmbientCallback = object : AmbientCallback() {
        override fun onExitAmbient() {
            super.onExitAmbient()
            currentLevelFragment?.setAmbientMode(
                AmbientSettings(
                    isAmbientMode = false,
                    isLowBitAmbient = false,
                )
            )
        }

        override fun onEnterAmbient(ambientDetails: Bundle?) {
            super.onEnterAmbient(ambientDetails)
            val lowBit = ambientDetails?.getBoolean(EXTRA_LOWBIT_AMBIENT) ?: true
            currentLevelFragment?.setAmbientMode(
                AmbientSettings(
                    true,
                    lowBit,
                )
            )
            updateClockText(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AmbientModeSupport.attach(this)

        bindViewModel()
        preferencesRepository.updateCustomGameMode(Minefield(12, 12, 25))

        loadGameFragment()
        viewModel.startNewGame(Difficulty.Custom)

        swipe.isSwipeable = false
    }

    override fun onResume() {
        super.onResume()
        clock.start {
            lifecycleScope.launchWhenResumed {
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
        eventObserver.observe(
            this@WatchGameActivity,
            {
                onGameEvent(it)
            }
        )
        elapsedTimeSeconds.observe(
            this@WatchGameActivity,
            {
                // Nothing
            }
        )
        mineCount.observe(
            this@WatchGameActivity,
            {
                if (it > 0) {
                    messageText.text = applicationContext.getString(R.string.mines_remaining, it)
                }
            }
        )
        difficulty.observe(
            this@WatchGameActivity,
            {
                // Nothing
            }
        )
    }

    private fun onGameEvent(event: Event) {
        when (event) {
            Event.StartNewGame -> {
                status = Status.PreGame
                newGame.visibility = View.GONE
            }
            Event.Resume, Event.Running -> {
                status = Status.Running
                newGame.visibility = View.GONE
            }
            Event.Victory -> {
                status = Status.Over()
                messageText.text = getString(R.string.victory)
                waitAndShowNewGameButton()
            }
            Event.GameOver -> {
                status = Status.Over()
                viewModel.stopClock()

                lifecycleScope.launch(context = Dispatchers.Main) {
                    messageText.text = getString(R.string.game_over)
                    waitAndShowNewGameButton()
                }
            }
            else -> {
            }
        }
    }

    private fun waitAndShowNewGameButton(wait: Long = DateUtils.SECOND_IN_MILLIS) {
        lifecycleScope.launch {
            delay(wait)
            if (status is Status.Over && !isFinishing) {
                newGame.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        it.visibility = View.GONE

                        viewModel.startNewGame(Difficulty.Custom)
                    }
                }
            }
        }
    }

    override fun getAmbientCallback(): AmbientCallback = ambientMode
}
