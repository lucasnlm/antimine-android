package dev.lucasnlm.antimine.wear.tutorial

import android.os.Bundle
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.wear.databinding.ActivityTutorialBinding

class WearTutorialActivity : ThemedActivity() {
    private val binding: ActivityTutorialBinding by lazy {
        ActivityTutorialBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}
