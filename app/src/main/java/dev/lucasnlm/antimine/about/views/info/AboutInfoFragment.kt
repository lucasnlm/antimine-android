package dev.lucasnlm.antimine.about.views.info

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dev.lucasnlm.antimine.BuildConfig
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.viewmodel.AboutEvent
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import kotlinx.android.synthetic.main.fragment_about_info.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AboutInfoFragment : Fragment(R.layout.fragment_about_info) {
    private val aboutViewModel: AboutViewModel by sharedViewModel()

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.about)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        version.text = getString(R.string.version_s, BuildConfig.VERSION_NAME)

        supportUs.setOnClickListener {
            aboutViewModel.sendEvent(AboutEvent.SupportUs)
        }

        thirdsParties.setOnClickListener {
            aboutViewModel.sendEvent(AboutEvent.ThirdPartyLicenses)
        }

        sourceCode.setOnClickListener {
            aboutViewModel.sendEvent(AboutEvent.SourceCode)
        }

        translation.setOnClickListener {
            aboutViewModel.sendEvent(AboutEvent.Translators)
        }
    }
}
