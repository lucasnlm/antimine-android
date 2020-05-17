package dev.lucasnlm.antimine.stats

import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import dev.lucasnlm.antimine.R

class StatsActivity : DaggerAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty)
        setTitle(R.string.events)
    }
}
