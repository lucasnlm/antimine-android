package dev.lucasnlm.antimine.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.ThematicActivity
import dev.lucasnlm.antimine.about.viewmodel.AboutEvent
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import dev.lucasnlm.antimine.about.views.AboutInfoFragment
import dev.lucasnlm.antimine.about.views.licenses.LicensesFragment
import dev.lucasnlm.antimine.about.views.translators.TranslatorsFragment
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AboutActivity : ThematicActivity(R.layout.activity_empty) {
    private val aboutViewModel: AboutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        replaceFragment(AboutInfoFragment(), null)

        lifecycleScope.launchWhenCreated {
            aboutViewModel.observeEvent().collect { event ->
                when (event) {
                    AboutEvent.ThirdPartyLicenses -> {
                        replaceFragment(LicensesFragment(), LicensesFragment.TAG)
                    }
                    AboutEvent.SourceCode -> {
                        openSourceCode()
                    }
                    AboutEvent.Translators -> {
                        replaceFragment(TranslatorsFragment(), TranslatorsFragment.TAG)
                    }
                }
            }
        }
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
