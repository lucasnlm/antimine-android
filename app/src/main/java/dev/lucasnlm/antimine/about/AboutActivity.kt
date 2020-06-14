package dev.lucasnlm.antimine.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.views.AboutInfoFragment
import dev.lucasnlm.antimine.about.models.AboutEvent
import dev.lucasnlm.antimine.about.views.translators.TranslatorsFragment
import dev.lucasnlm.antimine.about.views.thirds.ThirdPartiesFragment
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel

@AndroidEntryPoint
class AboutActivity : AppCompatActivity() {
    private lateinit var aboutViewModel: AboutViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty)
        setTitle(R.string.about)

        aboutViewModel = ViewModelProviders.of(this).get(AboutViewModel::class.java)

        aboutViewModel.eventObserver.observe(
            this,
            Observer { event ->
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
            }
        )

        replaceFragment(AboutInfoFragment(), null)
    }

    private fun replaceFragment(fragment: Fragment, stackName: String?) {
        supportFragmentManager.beginTransaction().apply {
            if (stackName != null) {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(stackName)
            }
            replace(R.id.content, fragment)
        }.commitAllowingStateLoss()
    }

    private fun openSourceCode() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_CODE)))
    }

    companion object {
        private const val SOURCE_CODE = "https://github.com/lucasnlm/antimine-android"
    }
}
