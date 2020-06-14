package dev.lucasnlm.antimine.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.history.views.HistoryFragment

@AndroidEntryPoint
class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty)
        setTitle(R.string.previous_games)

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.content, HistoryFragment())
        }.commitAllowingStateLoss()
    }
}
