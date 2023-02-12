package dev.lucasnlm.antimine.wear.main

import android.content.Intent
import android.os.Bundle
import androidx.wear.widget.WearableLinearLayoutManager
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.wear.R
import dev.lucasnlm.antimine.wear.databinding.ActivityDifficultyBinding
import dev.lucasnlm.antimine.wear.game.GameActivity
import dev.lucasnlm.antimine.wear.main.models.MenuItem
import dev.lucasnlm.antimine.wear.main.view.MainMenuAdapter

class DifficultyActivity : ThemedActivity() {
    private lateinit var binding: ActivityDifficultyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDifficultyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val difficultyList = mapOf(
            Difficulty.Beginner to R.string.beginner,
            Difficulty.Intermediate to R.string.intermediate,
            Difficulty.Expert to R.string.expert,
            Difficulty.Master to R.string.master,
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

        binding.close.setOnClickListener {
            finish()
        }
    }

    private fun startGameOnDifficulty(difficulty: Difficulty) {
        val context = application.applicationContext
        val intent = Intent(context, GameActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            val bundle = Bundle().apply {
                putSerializable(GameActivity.DIFFICULTY, difficulty)
            }
            putExtras(bundle)
        }
        context.startActivity(intent)
    }
}
