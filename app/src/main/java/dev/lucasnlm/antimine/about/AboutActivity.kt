package dev.lucasnlm.antimine.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import dev.lucasnlm.antimine.BuildConfig
import dev.lucasnlm.antimine.R

import dev.lucasnlm.antimine.about.thirds.ThirdPartiesActivity
import dev.lucasnlm.antimine.about.translators.TranslatorsActivity
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        bindToolbar()

        version.text = getString(
            R.string.version_s,
            getString(R.string.app_name), BuildConfig.VERSION_NAME
        )

        thirdsParties.setOnClickListener { openThirdParties() }
        sourceCode.setOnClickListener { openSourceCode() }
        translation.setOnClickListener { openTranslation() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun bindToolbar() {
        supportActionBar?.apply {
            setTitle(R.string.about)
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    private fun openThirdParties() {
        startActivity(Intent(this, ThirdPartiesActivity::class.java))
    }

    private fun openTranslation() {
        startActivity(Intent(this, TranslatorsActivity::class.java))
    }

    private fun openSourceCode() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_CODE)))
    }

    companion object {
        private const val SOURCE_CODE = "https://github.com/lucasnlm/antimine-android"
    }
}
