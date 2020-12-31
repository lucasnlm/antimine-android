package dev.lucasnlm.antimine.history

import android.os.Bundle
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.ui.ThematicActivity
import dev.lucasnlm.antimine.history.views.HistoryFragment

class HistoryActivity : ThematicActivity(R.layout.activity_empty) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.content, HistoryFragment())
        }.commitAllowingStateLoss()
    }
}
