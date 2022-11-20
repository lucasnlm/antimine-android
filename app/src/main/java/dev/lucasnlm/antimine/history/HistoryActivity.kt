package dev.lucasnlm.antimine.history

import android.os.Bundle
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.appbar.MaterialToolbar
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.history.views.HistoryFragment
import dev.lucasnlm.antimine.ui.ext.ThematicActivity

class HistoryActivity : ThematicActivity(R.layout.activity_history) {
    private val toolbar: MaterialToolbar by lazy {
        findViewById(R.id.toolbar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar.apply {
            setSupportActionBar(this)
            setNavigationOnClickListener {
                finish()
            }
        }

        supportFragmentManager.commit(allowStateLoss = true) {
            replace<HistoryFragment>(R.id.content)
        }
    }
}
