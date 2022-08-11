package dev.lucasnlm.antimine.history

import android.os.Bundle
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.history.views.HistoryFragment
import dev.lucasnlm.antimine.ui.ext.ThematicActivity
import kotlinx.android.synthetic.main.activity_stats.*

class HistoryActivity : ThematicActivity(R.layout.activity_history) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        section.bind(
            text = R.string.previous_games,
            startButton = R.drawable.back_arrow,
            startDescription = R.string.back,
            startAction = {
                finish()
            },
        )

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.content, HistoryFragment())
        }.commitAllowingStateLoss()
    }
}
