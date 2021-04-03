package dev.lucasnlm.antimine.tutorial

import android.content.Intent
import android.os.Bundle
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.ui.ThematicActivity
import kotlinx.android.synthetic.main.tutorial_activity.*

class TutorialActivity : ThematicActivity(R.layout.tutorial_activity) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        section.bind(
            text = R.string.tutorial,
            startButton = R.drawable.back_arrow,
            startDescription = R.string.back,
            startAction = {
                finish()
            }
        )

        playGame.bind(
            theme = usingTheme,
            text = R.string.start,
            invert = true,
            centralize = true,
            onAction = {
                finish()
                val intent = Intent(this, Class.forName("dev.lucasnlm.antimine.GameActivity")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    val bundle = Bundle().apply {
                        putSerializable("difficulty", Difficulty.Beginner)
                    }
                    putExtras(bundle)
                }
                startActivity(intent)
            }
        )
    }
}
