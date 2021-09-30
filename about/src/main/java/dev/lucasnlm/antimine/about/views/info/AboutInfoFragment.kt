package dev.lucasnlm.antimine.about.views.info

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import dev.lucasnlm.antimine.about.R
import dev.lucasnlm.antimine.about.viewmodel.AboutEvent
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import kotlinx.android.synthetic.main.fragment_about_info.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AboutInfoFragment : Fragment(R.layout.fragment_about_info) {
    private val aboutViewModel: AboutViewModel by sharedViewModel()
    private val themeRepository: IThemeRepository by inject()

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.about)
    }

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

        tutorial.bind(
            theme = themeRepository.getTheme(),
            text = R.string.tutorial,
            centralize = true,
            onAction = {
                aboutViewModel.sendEvent(AboutEvent.Tutorial)
            }
        )

        thirdsParties.bind(
            theme = themeRepository.getTheme(),
            text = R.string.show_licenses,
            centralize = true,
            onAction = {
                aboutViewModel.sendEvent(AboutEvent.ThirdPartyLicenses)
            }
        )

        translation.bind(
            theme = themeRepository.getTheme(),
            text = R.string.translation,
            centralize = true,
            onAction = {
                aboutViewModel.sendEvent(AboutEvent.Translators)
            }
        )

        sourceCode.bind(
            theme = themeRepository.getTheme(),
            text = R.string.source_code,
            centralize = true,
            onAction = {
                aboutViewModel.sendEvent(AboutEvent.SourceCode)
            }
        )
    }

    companion object {
        private const val INSTANT_BUILD_FLAVOR = "com.google.android.gms.instant.flavor"
    }
}
