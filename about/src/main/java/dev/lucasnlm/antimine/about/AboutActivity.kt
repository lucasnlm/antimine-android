package dev.lucasnlm.antimine.about

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import dev.lucasnlm.antimine.ui.ThematicActivity
import dev.lucasnlm.antimine.about.viewmodel.AboutEvent
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import dev.lucasnlm.antimine.about.views.info.AboutInfoFragment
import dev.lucasnlm.antimine.about.views.licenses.LicensesFragment
import dev.lucasnlm.antimine.about.views.translators.TranslatorsFragment
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class AboutActivity : ThematicActivity(R.layout.activity_empty) {
    private val aboutViewModel: AboutViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        replaceFragment(AboutInfoFragment(), null)

        lifecycleScope.launchWhenCreated {
            aboutViewModel.observeEvent().collect { event ->
                when (event) {
                    AboutEvent.ThirdPartyLicenses -> {
                        replaceFragment(LicensesFragment(), LicensesFragment.TAG)
                    }
                    AboutEvent.Translators -> {
                        replaceFragment(TranslatorsFragment(), TranslatorsFragment.TAG)
                    }
                    else -> {}
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
}
