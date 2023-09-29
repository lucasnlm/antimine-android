package dev.lucasnlm.antimine.history

import android.os.Bundle
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import dev.lucasnlm.antimine.databinding.ActivityHistoryBinding
import dev.lucasnlm.antimine.history.views.HistoryFragment
import dev.lucasnlm.antimine.ui.ext.ThemedActivity

class HistoryActivity : ThemedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindToolbar(binding.toolbar)

        supportFragmentManager.commit(allowStateLoss = true) {
            replace<HistoryFragment>(binding.content.id)
        }
    }
}
