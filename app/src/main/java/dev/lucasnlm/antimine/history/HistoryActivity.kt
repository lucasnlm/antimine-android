package dev.lucasnlm.antimine.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.history.views.HistoryFragment

@AndroidEntryPoint
class HistoryActivity : AppCompatActivity(R.layout.activity_empty) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.content, HistoryFragment())
        }.commitAllowingStateLoss()
    }
}
