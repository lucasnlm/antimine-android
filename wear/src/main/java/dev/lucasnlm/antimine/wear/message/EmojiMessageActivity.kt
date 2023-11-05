package dev.lucasnlm.antimine.wear.message

import android.content.Intent
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.wear.databinding.ActivityGameOverBinding
import dev.lucasnlm.antimine.wear.game.WearGameActivity

abstract class EmojiMessageActivity : ThemedActivity() {
    @get:StringRes
    abstract val message: Int

    @get:DrawableRes
    abstract val emojiRes: Int

    private val binding: ActivityGameOverBinding by lazy {
        ActivityGameOverBinding.inflate(layoutInflater)
    }

    private fun newGame() {
        val context = application.applicationContext
        val intent =
            Intent(context, WearGameActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(WearGameActivity.NEW_GAME, "true")
            }
        context.startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.image.setImageResource(emojiRes)

        binding.message.text = getString(message)

        binding.action.setOnClickListener {
            newGame()
        }
    }
}
