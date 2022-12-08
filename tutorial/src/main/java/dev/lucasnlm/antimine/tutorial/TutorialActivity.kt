package dev.lucasnlm.antimine.tutorial

import android.content.Intent
import android.os.Bundle
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.ui.ext.ThematicActivity
import dev.lucasnlm.external.IAnalyticsManager
import kotlinx.android.synthetic.main.tutorial_activity.*
import org.koin.android.ext.android.inject

class TutorialActivity : ThematicActivity(R.layout.tutorial_activity) {
    private val preferencesRepository: IPreferencesRepository by inject()
    private val analyticsManager: IAnalyticsManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesRepository.setTutorialDialog(false)
        analyticsManager.sentEvent(Analytics.OpenTutorial)

        bindToolbar(toolbar)

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
            },
        )
    }
}
