package dev.lucasnlm.antimine.wear.main

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.wear.widget.WearableLinearLayoutManager
import dev.lucasnlm.antimine.common.level.repository.SavesRepository
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.wear.R
import dev.lucasnlm.antimine.wear.databinding.ActivityMainBinding
import dev.lucasnlm.antimine.wear.game.WearGameActivity
import dev.lucasnlm.antimine.wear.main.models.MenuItem
import dev.lucasnlm.antimine.wear.main.view.MainMenuAdapter
import dev.lucasnlm.antimine.wear.tutorial.WearTutorialActivity
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import dev.lucasnlm.antimine.i18n.R as i18n

class MainActivity : ThemedActivity() {
    private val preferencesRepository: PreferencesRepository by inject()
    private val savesRepository: SavesRepository by inject()
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!preferencesRepository.showContinueGame()) {
            lifecycleScope.launch {
                savesRepository.fetchCurrentSave()?.let {
                    preferencesRepository.setContinueGameLabel(true)
                }
            }
        }

        setContentView(binding.root)

        val menuList =
            listOf(
                MenuItem(
                    id = 0L,
                    label =
                        if (preferencesRepository.showContinueGame()) {
                            i18n.string.continue_game
                        } else {
                            i18n.string.start
                        },
                    icon = R.drawable.play,
                    onClick = {
                        continueGame()
                    },
                ),
                MenuItem(
                    id = 1L,
                    label = i18n.string.minefield,
                    icon = R.drawable.add,
                    onClick = {
                        startDifficultyScreen()
                    },
                ),
                MenuItem(
                    id = 2L,
                    label = i18n.string.control_types,
                    icon = R.drawable.control,
                    onClick = {
                        startControlScreen()
                    },
                ),
                MenuItem(
                    id = 3L,
                    label = i18n.string.themes,
                    icon = R.drawable.themes,
                    onClick = {
                        startThemeScreen()
                    },
                ),
                MenuItem(
                    id = 4L,
                    label = i18n.string.tutorial,
                    icon = R.drawable.tutorial,
                    onClick = {
                        startTutorial()
                    },
                ),
                MenuItem(
                    id = 6L,
                    label = i18n.string.quit,
                    icon = R.drawable.close,
                    onClick = {
                        finishAffinity()
                    },
                ),
            )

        binding.recyclerView.apply {
            setHasFixedSize(true)
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(this@MainActivity)
            adapter = MainMenuAdapter(menuList)
        }
    }

    private fun continueGame(difficulty: Difficulty? = null) {
        val context = application.applicationContext
        val intent =
            Intent(context, WearGameActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                difficulty?.let {
                    val bundle =
                        Bundle().apply {
                            putSerializable(WearGameActivity.DIFFICULTY, it)
                        }
                    putExtras(bundle)
                }
            }
        context.startActivity(intent)
    }

    private fun startDifficultyScreen() {
        val intent = Intent(this, DifficultyActivity::class.java)
        startActivity(intent)
    }

    private fun startControlScreen() {
        val intent = Intent(this, ControlTypeActivity::class.java)
        startActivity(intent)
    }

    private fun startThemeScreen() {
        val intent = Intent(this, WearThemeActivity::class.java)
        startActivity(intent)
    }

    private fun startTutorial() {
        val intent = Intent(this, WearTutorialActivity::class.java)
        startActivity(intent)
    }
}
