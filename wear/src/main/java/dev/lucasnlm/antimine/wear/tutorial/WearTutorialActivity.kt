package dev.lucasnlm.antimine.wear.tutorial

import android.os.Bundle
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.wear.databinding.ActivityTutorialBinding

class WearTutorialActivity : ThemedActivity() {
    private lateinit var binding: ActivityTutorialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
