package dev.lucasnlm.antimine.history

import android.os.Bundle
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.history.views.HistoryFragment
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import kotlinx.android.synthetic.main.activity_history.*

class HistoryActivity : ThemedActivity(R.layout.activity_history) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindToolbar(toolbar)

        supportFragmentManager.commit(allowStateLoss = true) {
            replace<HistoryFragment>(R.id.content)
        }
    }
}
