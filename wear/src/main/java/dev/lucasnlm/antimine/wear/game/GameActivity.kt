package dev.lucasnlm.antimine.wear.game

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import dev.lucasnlm.antimine.common.level.view.GameRenderFragment
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.serializableNonSafe
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.wear.R
import dev.lucasnlm.antimine.wear.databinding.ActivityGameBinding
import dev.lucasnlm.antimine.wear.message.GameOverActivity
import dev.lucasnlm.antimine.wear.message.VictoryActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameActivity : ThemedActivity(), AndroidFragmentApplication.Callbacks {
    private lateinit var binding: ActivityGameBinding

    private val gameViewModel by viewModel<GameViewModel>()
    private val preferencesRepository: PreferencesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback {
            finish()
        }

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.levelContainer.apply {
            isSwipeable = false
            setBackButtonDismissible(true)
        }

        loadGameFragment()
        bindViewModel()
    }

    override fun onResume() {
        super.onResume()
        refreshSwitchButtons()
    }

    private fun refreshSwitchButtons() {
        val enabled = preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen
        val visibilityValue = if (enabled) { View.VISIBLE } else { View.GONE }

        binding.close.setOnClickListener {
            finish()
        }

        binding.selectOpen.apply {
            visibility = visibilityValue
            alpha = if (preferencesRepository.getSwitchControlAction() == Action.OpenTile) 1.0f else 0.5f
            setOnClickListener {
                gameViewModel.changeSwitchControlAction(Action.OpenTile)
            }
        }

        binding.selectFlag.apply {
            visibility = visibilityValue
            alpha = if (preferencesRepository.getSwitchControlAction() == Action.SwitchMark) 1.0f else 0.5f
            setOnClickListener {
                gameViewModel.changeSwitchControlAction(Action.SwitchMark)
            }
        }
    }

    private fun loadGameFragment() {
        supportFragmentManager.commit(allowStateLoss = true) {
            replace(binding.levelContainer.id, GameRenderFragment())
            handleIntent(intent)
        }
    }

    private fun handleIntent(intent: Intent) {
        lifecycleScope.launch {
            val extras = intent.extras ?: Bundle()
            val queryParamDifficulty = intent.data?.getQueryParameter("difficulty")
            when {
                queryParamDifficulty != null -> {
                    val upperDifficulty = queryParamDifficulty.uppercase()
                    val difficulty = Difficulty.values().firstOrNull { it.id == upperDifficulty }
                    if (difficulty == null) {
                        gameViewModel.loadLastGame()
                    } else {
                        gameViewModel.startNewGame(difficulty)
                    }
                }
                extras.containsKey(DIFFICULTY) -> {
                    intent.removeExtra(DIFFICULTY)
                    val difficulty = extras.serializableNonSafe<Difficulty>(DIFFICULTY)
                    gameViewModel.startNewGame(difficulty)
                }
                extras.containsKey(NEW_GAME) -> {
                    intent.removeExtra(NEW_GAME)
                    gameViewModel.startNewGame()
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

    private fun bindViewModel() = gameViewModel.apply {
        lifecycleScope.launchWhenCreated {
            observeState().collect {
                if (it.turn == 0 && (it.saveId == 0L || it.isLoadingMap || it.isCreatingGame)) {
                    binding.tapToBegin.apply {
                        text = when {
                            it.isCreatingGame -> {
                                getString(R.string.creating_valid_game)
                            }
                            it.isLoadingMap -> {
                                getString(R.string.loading)
                            }
                            else -> {
                                getString(R.string.tap_to_begin)
                            }
                        }
                        visibility = View.VISIBLE
                    }
                } else {
                    binding.tapToBegin.visibility = View.GONE
                }

                if (it.isCreatingGame) {
                    launch {
                        // Show loading indicator only when it takes more than:
                        delay(500)
                        if (singleState().isCreatingGame) {
                            binding.loadingGame.show()
                        }
                    }
                } else if (binding.loadingGame.isVisible) {
                    binding.loadingGame.hide()
                }

                if (it.duration % 10 > 2) {
                    binding.timer.apply {
                        visibility = if (!preferencesRepository.showTimer()) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }

                        alpha = 0.7f
                        text = DateUtils.formatElapsedTime(it.duration)
                    }
                } else if (it.duration > 0) {
                    binding.timer.apply {
                        text = getString(R.string.mines_remaining, it.mineCount)
                    }
                } else {
                    binding.timer.visibility = View.GONE
                }

                if (it.isGameCompleted) {
                    binding.newGame.setOnClickListener {
                        lifecycleScope.launch {
                            gameViewModel.startNewGame()
                        }
                    }
                    binding.newGame.visibility = View.VISIBLE
                } else {
                    binding.newGame.visibility = View.GONE
                }

                keepScreenOn(it.isActive)
                refreshSwitchButtons()
            }
        }

        lifecycleScope.launchWhenCreated {
            gameViewModel.observeSideEffects().collect {
                when (it) {
                    is GameEvent.ShowNoGuessFailWarning -> {
//                        warning = Snackbar.make(
//                            findViewById(android.R.id.content),
//                            R.string.no_guess_fail_warning,
//                            Snackbar.LENGTH_INDEFINITE,
//                        ).apply {
//                            setAction(R.string.ok) {
//                                warning?.dismiss()
//                            }
//                            show()
//                        }
                    }
                    is GameEvent.ShowNewGameDialog -> {
//                        lifecycleScope.launch {
//                            GameOverDialogFragment.newInstance(
//                                gameResult = GameResult.Completed,
//                                showContinueButton = gameViewModel.hasUnknownMines(),
//                                rightMines = 0,
//                                totalMines = 0,
//                                time = singleState().duration,
//                                received = 0,
//                                turn = -1,
//                            ).run {
//                                showAllowingStateLoss(supportFragmentManager, WinGameDialogFragment.TAG)
//                            }
//                        }
                    }
                    is GameEvent.VictoryDialog -> {
                        val intent = Intent(applicationContext, VictoryActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                    }
                    is GameEvent.GameOverDialog -> {
                        val intent = Intent(applicationContext, GameOverActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                    }
                    is GameEvent.GameCompleteDialog -> {
                        val intent = Intent(applicationContext, VictoryActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                    }
                    else -> {
                        // Empty
                    }
                }
            }
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
        const val NEW_GAME = "new_game"
        const val DIFFICULTY = "difficulty"
        const val START_GAME = "start_game"
        const val RETRY_GAME = "retry_game"
    }
}
