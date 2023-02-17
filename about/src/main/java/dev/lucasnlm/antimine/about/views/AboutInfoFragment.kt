package dev.lucasnlm.antimine.about.views

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import dev.lucasnlm.antimine.about.R
import dev.lucasnlm.antimine.about.databinding.FragmentAboutInfoBinding
import dev.lucasnlm.antimine.about.viewmodel.AboutEvent
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AboutInfoFragment : Fragment() {
    private lateinit var binding: FragmentAboutInfoBinding
    private val aboutViewModel: AboutViewModel by sharedViewModel()
    private val unknownVersionName = "?.?.?"

    private fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int = 0): PackageInfo? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION")
                getPackageInfo(packageName, flags)
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
                @Suppress("DEPRECATION")
                getApplicationInfo(packageName, flags)
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAboutInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context
        val packageManager = context.packageManager
        val packageName = context.packageName
        val versionName = packageManager.getPackageInfoCompat(packageName, 0)?.versionName ?: unknownVersionName
        binding.version.text = getString(R.string.version_s, versionName)
        binding.instant.isVisible = view.context.run {
            try {
                val info = packageManager.getApplicationInfoCompat(packageName, PackageManager.GET_META_DATA)
                val bundle: Bundle? = info?.metaData
                val appId = bundle?.getInt(INSTANT_BUILD_FLAVOR, 0) ?: 0
                appId > 0
            } catch (e: Exception) {
                false
            }
        }

        binding.tutorial.setOnClickListener {
            aboutViewModel.sendEvent(AboutEvent.Tutorial)
        }

        binding.thirdsParties.setOnClickListener {
            aboutViewModel.sendEvent(AboutEvent.ThirdPartyLicenses)
        }

        binding.translation.setOnClickListener {
            aboutViewModel.sendEvent(AboutEvent.Translators)
        }

        binding.sourceCode.setOnClickListener {
            aboutViewModel.sendEvent(AboutEvent.SourceCode)
        }
    }

    companion object {
        private const val INSTANT_BUILD_FLAVOR = "com.google.android.gms.instant.flavor"
    }
}
