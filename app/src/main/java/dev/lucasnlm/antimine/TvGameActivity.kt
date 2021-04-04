package dev.lucasnlm.antimine

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.gameover.GameOverDialogFragment
import dev.lucasnlm.antimine.gameover.WinGameDialogFragment
import dev.lucasnlm.antimine.gameover.model.GameResult
import dev.lucasnlm.antimine.common.level.view.GdxLevelFragment
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.ui.ThematicActivity
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IFeatureFlagManager
import dev.lucasnlm.external.IInstantAppManager
import dev.lucasnlm.external.ReviewWrapper
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_game.minesCount
import kotlinx.android.synthetic.main.activity_game.timer
import kotlinx.android.synthetic.main.activity_game_tv.*
import kotlinx.android.synthetic.main.activity_game_tv.controlsToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TvGameActivity :
    ThematicActivity(R.layout.activity_game_tv),
    DialogInterface.OnDismissListener,
    AndroidFragmentApplication.Callbacks {

    private val gameViewModel by viewModel<GameViewModel>()
    private val preferencesRepository: IPreferencesRepository by inject()
    private val analyticsManager: IAnalyticsManager by inject()
    private val instantAppManager: IInstantAppManager by inject()
    private val savesRepository: ISavesRepository by inject()
    private val reviewWrapper: ReviewWrapper by inject()
    private val featureFlagManager: IFeatureFlagManager by inject()

    private val areaSizeMultiplier by lazy { preferencesRepository.squareSize() }
    private val currentRadius by lazy { preferencesRepository.squareRadius() }
    private val useHelp by lazy { preferencesRepository.useHelp() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindViewModel()
        loadGameFragment()
        bindToast()

        gameViewModel.startNewGame()

        onOpenAppActions()
    }

    private fun bindToast() {
        val color = usingTheme.palette.background.toAndroidColor(168)
        val tint = ColorStateList.valueOf(color)
        gameToast.apply {
            visibility = View.VISIBLE
            backgroundTintList = tint
        }
    }

    private fun bindViewModel() = gameViewModel.apply {
        lifecycleScope.launchWhenCreated {
            observeState().collect {
                timer.apply {
                    visibility = if (it.duration == 0L) View.GONE else View.VISIBLE
                    text = DateUtils.formatElapsedTime(it.duration)
                }

                minesCount.apply {
                    visibility = View.VISIBLE
                    text = it.mineCount.toString()
                }

                if (it.turn < 3 && it.saveId == 0L) {
                    val color = usingTheme.palette.background.toAndroidColor(168)
                    val tint = ColorStateList.valueOf(color)
                    val controlText = gameViewModel.getControlDescription(applicationContext)

                    if (controlText != null && controlText.isNotBlank()) {
                        controlsToast.apply {
                            visibility = View.VISIBLE
                            backgroundTintList = tint
                            this.text = controlText
                        }
                    } else {
                        controlsToast.visibility = View.GONE
                    }
                } else {
                    controlsToast.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            lifecycleScope.launchWhenCreated {
                gameViewModel.observeSideEffects().collect {
                    when (it) {
                        is GameEvent.ShowNewGameDialog -> {
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
                        is GameEvent.VictoryDialog -> {
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
                        is GameEvent.GameOverDialog -> {
                            delay(it.delayToShow)
                            GameOverDialogFragment.newInstance(
                                gameResult = GameResult.GameOver,
                                showContinueButton = true,
                                rightMines = it.rightMines,
                                totalMines = it.totalMines,
                                time = it.timestamp,
                                received = it.receivedTips,
                                turn = -1,
                            ).run {
                                showAllowingStateLoss(supportFragmentManager, WinGameDialogFragment.TAG)
                            }
                        }
                        is GameEvent.GameCompleteDialog -> {
                            delay(it.delayToShow)
                            GameOverDialogFragment.newInstance(
                                gameResult = GameResult.Completed,
                                showContinueButton = false,
                                rightMines = it.rightMines,
                                totalMines = it.totalMines,
                                time = it.timestamp,
                                received = it.receivedTips,
                                turn = -1,
                            ).run {
                                showAllowingStateLoss(supportFragmentManager, WinGameDialogFragment.TAG)
                            }
                        }
                        else -> {
                            // Empty
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val willReset = restartIfNeed()

        if (!willReset) {
            gameViewModel.run {
                refreshUserPreferences()
                resumeGame()
            }

            analyticsManager.sentEvent(Analytics.Resume)
        }
    }

    override fun onPause() {
        super.onPause()

        gameViewModel.pauseGame()

        if (isFinishing) {
            analyticsManager.sentEvent(Analytics.Quit)
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

    private fun loadGameFragment() {
        supportFragmentManager.apply {
            beginTransaction().apply {
                replace(R.id.levelContainer, GdxLevelFragment(), GdxLevelFragment.TAG)
                commitAllowingStateLoss()
            }
        }
    }

    private fun restartIfNeed(): Boolean {
        return (
            areaSizeMultiplier != preferencesRepository.squareSize() ||
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

    override fun exit() {
        // Empty
    }

    override fun onDismiss(dialog: DialogInterface?) {
        gameViewModel.run {
            refreshUserPreferences()
            resumeGame()
        }
    }

    companion object {
        val TAG = TvGameActivity::class.simpleName
    }
}
