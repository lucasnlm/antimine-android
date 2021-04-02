package dev.lucasnlm.antimine.tutorial

import android.os.Bundle
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
            }
        )
    }
}
