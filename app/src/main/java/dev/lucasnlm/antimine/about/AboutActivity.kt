package dev.lucasnlm.antimine.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.views.AboutInfoFragment
import dev.lucasnlm.antimine.about.models.AboutEvent
import dev.lucasnlm.antimine.about.views.translators.TranslatorsFragment
import dev.lucasnlm.antimine.about.views.thirds.ThirdPartiesFragment
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel

class AboutActivity : AppCompatActivity() {
    private lateinit var aboutViewModel: AboutViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        bindToolbar()

        aboutViewModel = ViewModelProviders.of(this).get(AboutViewModel::class.java)

        aboutViewModel.eventObserver.observe(this, Observer { event ->
            when (event) {
                AboutEvent.ThirdPartyLicenses -> {
                    replaceFragment(ThirdPartiesFragment(), ThirdPartiesFragment::class.simpleName)
                }
                AboutEvent.SourceCode -> {
                    openSourceCode()
                }
                AboutEvent.Translators -> {
                    replaceFragment(TranslatorsFragment(), TranslatorsFragment::class.simpleName)
                }
                else -> {
                    replaceFragment(AboutInfoFragment(), null)
                }
            }
        })

        replaceFragment(AboutInfoFragment(), null)
    }

    private fun replaceFragment(fragment: Fragment, stackName: String?) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.content, fragment)
            if (stackName != null) {
                addToBackStack(stackName)
            }
        }.commitAllowingStateLoss()
    }

    private fun openSourceCode() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_CODE)))
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

    companion object {
        private const val SOURCE_CODE = "https://github.com/lucasnlm/antimine-android"
    }
}
