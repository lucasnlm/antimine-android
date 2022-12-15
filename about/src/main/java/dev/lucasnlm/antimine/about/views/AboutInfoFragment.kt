package dev.lucasnlm.antimine.about.views

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import dev.lucasnlm.antimine.about.R
import dev.lucasnlm.antimine.about.viewmodel.AboutEvent
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import kotlinx.android.synthetic.main.fragment_about_info.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AboutInfoFragment : Fragment(R.layout.fragment_about_info) {
    private val aboutViewModel: AboutViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val versionName = requireContext().run {
            packageManager.getPackageInfo(packageName, 0).versionName
        }
        version.text = getString(R.string.version_s, versionName)
        instant.isVisible = view.context.run {
            try {
                val info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                val bundle: Bundle = info.metaData
                val appId = bundle.getInt(INSTANT_BUILD_FLAVOR, 0)
                appId > 0
            } catch (e: Exception) {
                false
            }
        }

        tutorial.setOnClickListener {
            aboutViewModel.sendEvent(AboutEvent.Tutorial)
        }

        thirdsParties.setOnClickListener {
            aboutViewModel.sendEvent(AboutEvent.ThirdPartyLicenses)
        }

        translation.setOnClickListener {
            aboutViewModel.sendEvent(AboutEvent.Translators)
        }

        sourceCode.setOnClickListener {
            aboutViewModel.sendEvent(AboutEvent.SourceCode)
        }
    }

    companion object {
        private const val INSTANT_BUILD_FLAVOR = "com.google.android.gms.instant.flavor"
    }
}
