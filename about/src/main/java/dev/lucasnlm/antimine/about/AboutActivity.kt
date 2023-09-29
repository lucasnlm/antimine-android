package dev.lucasnlm.antimine.about

import android.os.Bundle
import androidx.fragment.app.commit
import dev.lucasnlm.antimine.about.databinding.ActivityContainerBinding
import dev.lucasnlm.antimine.about.views.AboutInfoFragment
import dev.lucasnlm.antimine.ui.ext.ThemedActivity

class AboutActivity : ThemedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindToolbar(binding.toolbar)

        supportFragmentManager.commit(allowStateLoss = true) {
            replace(binding.content.id, AboutInfoFragment())
        }
    }
}
