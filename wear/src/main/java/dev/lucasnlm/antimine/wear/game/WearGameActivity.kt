package dev.lucasnlm.antimine.wear.game

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.format.DateUtils
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import dev.lucasnlm.antimine.common.level.view.GameRenderFragment
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.preferences.PreferencesRepositoryImpl
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.utils.BundleExt.serializableNonSafe
import dev.lucasnlm.antimine.wear.databinding.ActivityGameBinding
import dev.lucasnlm.antimine.wear.message.GameOverActivity
import dev.lucasnlm.antimine.wear.message.VictoryActivity
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import dev.lucasnlm.antimine.i18n.R as i18n

class WearGameActivity : ThemedActivity(), AndroidFragmentApplication.Callbacks {
    private val gameViewModel by viewModel<GameViewModel>()
    private val preferencesRepository: PreferencesRepositoryImpl by inject()

    private val binding: ActivityGameBinding by lazy {
        ActivityGameBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback {
            finish()
        }

        setContentView(binding.root)

        loadGameFragment()
        bindViewModel()
    }

    @Suppress("UnnecessaryVariable")
    private fun View.tryHandleMotionEvent(event: MotionEvent?): Boolean {
        val shouldHandle =
            if (event == null) {
                false
            } else {
                val location = IntArray(2)
                getLocationOnScreen(location)
                val viewX = location[0]
                val viewY = location[1]

                val left = viewX
                val right = viewX + width
                val top = viewY
                val bottom = viewY + height

                val rect = Rect(left, top, right, bottom)
                rect.contains(event.x.toInt(), event.y.toInt())
            }

        return shouldHandle && dispatchTouchEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val handledByView =
            listOf(
                binding.close,
                binding.selectFlag,
                binding.selectOpen,
                binding.newGame,
            ).firstOrNull {
                it.tryHandleMotionEvent(event)
            } != null

        return handledByView || binding.levelContainer.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()
        refreshSwitchButtons()
    }

    private fun refreshSwitchButtons() {
        val enabled = preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen

        binding.close.setOnClickListener {
            finish()
        }

        binding.selectOpen.apply {
            isVisible = enabled
            alpha = if (gameViewModel.singleState().selectedAction == Action.OpenTile) 1.0f else 0.5f
            setOnClickListener {
                gameViewModel.changeSwitchControlAction(Action.OpenTile)
            }
        }

        binding.selectFlag.apply {
            isVisible = enabled
            alpha = if (gameViewModel.singleState().selectedAction == Action.SwitchMark) 1.0f else 0.5f
            setOnClickListener {
                gameViewModel.changeSwitchControlAction(Action.SwitchMark)
            }
        }
    }

    private fun loadGameFragment() {
        supportFragmentManager.commit(allowStateLoss = true) {
            val fragment = GameRenderFragment()
            replace(binding.levelContainer.id, fragment)
            handleIntent(intent)

            binding.levelContainer.setChildFragment(fragment)
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
                    val saveId = extras.getString(RETRY_GAME)
                    gameViewModel.retryGame(saveId.orEmpty())
                }
                extras.containsKey(START_GAME) -> {
                    val saveId = extras.getString(START_GAME)
                    gameViewModel.loadGame(saveId.orEmpty())
                }
                else -> {
                    gameViewModel.loadLastGame()
                }
            }
        }
    }

    private fun bindViewModel() =
        gameViewModel.apply {
            lifecycleScope.launch {
                observeState().collect {
                    if (it.turn == 0 && (it.saveId == null || it.isEngineLoading || it.isCreatingGame)) {
                        binding.tapToBegin.apply {
                            text =
                                when {
                                    it.isCreatingGame -> {
                                        getString(i18n.string.creating_valid_game)
                                    }
                                    it.isEngineLoading -> {
                                        getString(i18n.string.loading)
                                    }
                                    else -> {
                                        getString(i18n.string.tap_to_begin)
                                    }
                                }
                            isVisible = true
                        }
                    } else {
                        binding.tapToBegin.isVisible = false
                    }

                    if (it.duration % 10 > 2) {
                        binding.timer.apply {
                            isVisible = preferencesRepository.showTimer()
                            alpha = 0.7f
                            text = DateUtils.formatElapsedTime(it.duration)
                        }
                    } else if (it.duration > 0) {
                        binding.timer.apply {
                            text = getString(i18n.string.mines_remaining, it.mineCount)
                        }
                    } else {
                        binding.timer.isVisible = false
                    }

                    if (it.isGameCompleted) {
                        binding.newGame.setOnClickListener {
                            lifecycleScope.launch {
                                gameViewModel.startNewGame()
                            }
                        }
                        binding.newGame.isGone = false
                    } else {
                        binding.newGame.setOnClickListener(null)
                        binding.newGame.isGone = true
                    }

                    keepScreenOn(it.isActive)
                    refreshSwitchButtons()
                }
            }

            lifecycleScope.launch {
                gameViewModel.observeSideEffects().collect {
                    when (it) {
                        is GameEvent.ShowNoGuessFailWarning -> {}
                        is GameEvent.ShowNewGameDialog -> {}
                        is GameEvent.VictoryDialog -> {
                            val intent =
                                Intent(applicationContext, VictoryActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                            startActivity(intent)
                        }
                        is GameEvent.GameOverDialog -> {
                            val intent =
                                Intent(applicationContext, GameOverActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                            startActivity(intent)
                        }
                        is GameEvent.GameCompleteDialog -> {
                            val intent =
                                Intent(applicationContext, VictoryActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
