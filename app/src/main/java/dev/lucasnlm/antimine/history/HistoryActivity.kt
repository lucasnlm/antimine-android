package dev.lucasnlm.antimine.history

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.ThematicActivity
import dev.lucasnlm.antimine.history.views.HistoryFragment

@AndroidEntryPoint
class HistoryActivity : ThematicActivity(R.layout.activity_empty) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.content, HistoryFragment())
        }.commitAllowingStateLoss()
    }
}
