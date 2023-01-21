package dev.lucasnlm.antimine.about.views

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
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

    private val unknownVersionName = "?.?.?"

    private fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int = 0): PackageInfo? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
            }
        } catch (e: Exception) {
            null
        }
    }


    private fun PackageManager.getApplicationInfoCompat(packageName: String, flags: Int = 0): ApplicationInfo? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val applicationInfoFlags = PackageManager.ApplicationInfoFlags.of(flags.toLong())
                getApplicationInfo(packageName, applicationInfoFlags)
            } else {
                @Suppress("DEPRECATION") getApplicationInfo(packageName, flags)
            }
        } catch (e: Exception) {
            null
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context
        val packageManager = context.packageManager
        val packageName = context.packageName
        val versionName = packageManager.getPackageInfoCompat(packageName, 0)?.versionName ?: unknownVersionName
        version.text = getString(R.string.version_s, versionName)
        instant.isVisible = view.context.run {
            try {
                val info = packageManager.getApplicationInfoCompat(packageName, PackageManager.GET_META_DATA)
                val bundle: Bundle? = info?.metaData
                val appId = bundle?.getInt(INSTANT_BUILD_FLAVOR, 0) ?: 0
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
