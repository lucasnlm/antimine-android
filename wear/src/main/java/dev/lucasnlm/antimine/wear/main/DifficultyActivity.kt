package dev.lucasnlm.antimine.wear.main

import android.content.Intent
import android.os.Bundle
import androidx.wear.widget.WearableLinearLayoutManager
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.wear.R
import dev.lucasnlm.antimine.wear.databinding.ActivityDifficultyBinding
import dev.lucasnlm.antimine.wear.game.WearGameActivity
import dev.lucasnlm.antimine.wear.main.models.MenuItem
import dev.lucasnlm.antimine.wear.main.view.MainMenuAdapter
import dev.lucasnlm.antimine.i18n.R as i18n

class DifficultyActivity : ThemedActivity() {
    private val binding: ActivityDifficultyBinding by lazy {
        ActivityDifficultyBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val difficultyList =
            mapOf(
                Difficulty.Beginner to i18n.string.beginner,
                Difficulty.Intermediate to i18n.string.intermediate,
                Difficulty.Expert to i18n.string.expert,
                Difficulty.Master to i18n.string.master,
            ).entries.mapIndexed { index, entry ->
                MenuItem(
                    id = index.toLong(),
                    label = entry.value,
                    icon = R.drawable.round_arrow,
                    highlight = false,
                    onClick = {
                        startGameOnDifficulty(entry.key)
                    },
                )
            }

        binding.recyclerView.apply {
            setHasFixedSize(true)
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(this@DifficultyActivity)
            adapter = MainMenuAdapter(difficultyList)
        }
    }

    private fun startGameOnDifficulty(difficulty: Difficulty) {
        val context = application.applicationContext
        val intent =
            Intent(context, WearGameActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                val bundle =
                    Bundle().apply {
                        putSerializable(WearGameActivity.DIFFICULTY, difficulty)
                    }
                putExtras(bundle)
            }
        context.startActivity(intent)
    }
}
