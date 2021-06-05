package dev.lucasnlm.antimine.wear

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.View
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.view.GdxLevelFragment
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.ui.ThematicActivity
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.repository.Themes
import kotlinx.android.synthetic.main.activity_level.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class WatchGameActivity : ThematicActivity(R.layout.activity_level), AndroidFragmentApplication.Callbacks {
    private val viewModel by viewModel<GameViewModel>()
    private val preferencesRepository: IPreferencesRepository by inject()

    private val clock = Clock()
    private var lastShownTime: String? = null

    private val gameLevelFragment = GdxLevelFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        preferencesRepository.useTheme(Themes.darkLimeTheme().id)
        preferencesRepository.updateCustomGameMode(Minefield(12, 12, 25))

        super.onCreate(savedInstanceState)

        bindViewModel()
        loadGameFragment()

        lifecycleScope.launchWhenCreated {
            viewModel.startNewGame(Difficulty.Custom)
        }

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
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.level_container, gameLevelFragment)
            commitAllowingStateLoss()
        }
    }

    private fun bindViewModel() = viewModel.apply {
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

                messageText.apply {
                    val mineCount = it.mineCount
                    if (mineCount != null && mineCount < 0) {
                        visibility = View.VISIBLE
                        text.toString().toIntOrNull()?.let { oldValue ->
                            if (oldValue > mineCount) {
                                startAnimation(AnimationUtils.loadAnimation(context, R.anim.fast_shake))
                            }
                        }
                    } else {
                        visibility = View.GONE
                    }

                    text = applicationContext.getString(R.string.mines_remaining, it.mineCount)
                }

                if (it.isActive) {
                    newGame.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.observeSideEffects().collect {
                when (it) {
                    is GameEvent.ShowNewGameDialog -> {
                        newGame.visibility = View.VISIBLE
                    }
                    is GameEvent.VictoryDialog -> {
                        messageText.apply {
                            visibility = View.VISIBLE
                            text = getString(R.string.victory)
                        }
                        waitAndShowNewGameButton()
                    }
                    is GameEvent.GameOverDialog -> {
                        messageText.apply {
                            visibility = View.VISIBLE
                            text = getString(R.string.game_over)
                        }
                        waitAndShowNewGameButton()
                    }
                    is GameEvent.GameCompleteDialog -> {
                        newGame.visibility = View.VISIBLE
                    }
                    else -> {
                        // Empty
                    }
                }
            }
        }
    }

    private fun waitAndShowNewGameButton(wait: Long = DateUtils.SECOND_IN_MILLIS) {
        lifecycleScope.launch {
            delay(wait)
            if (!isFinishing) {
                newGame.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        it.visibility = View.GONE
                        lifecycleScope.launch {
                            viewModel.startNewGame()
                        }
                    }
                }
            }
        }
    }

    override fun exit() {
        // LibGDX exit callback
    }
}
