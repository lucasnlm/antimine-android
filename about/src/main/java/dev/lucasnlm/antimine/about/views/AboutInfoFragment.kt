package dev.lucasnlm.antimine.about.views

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import dev.lucasnlm.antimine.about.databinding.FragmentAboutInfoBinding
import dev.lucasnlm.antimine.about.viewmodel.AboutEvent
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.utils.BuildExt.androidTiramisu
import dev.lucasnlm.external.AnalyticsManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import dev.lucasnlm.antimine.i18n.R as i18n

class AboutInfoFragment : Fragment() {
    private val aboutViewModel: AboutViewModel by sharedViewModel()
    private val audioManager: GameAudioManager by inject()
    private val analyticsManager: AnalyticsManager by inject()
    private val unknownVersionName = "?.?.?"

    private lateinit var binding: FragmentAboutInfoBinding

    private fun PackageManager.getPackageInfoCompat(
        packageName: String,
        flags: Int = 0,
    ): PackageInfo? {
        return runCatching {
            when {
                androidTiramisu() -> {
                    getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
                }
                else -> {
                    getPackageInfo(packageName, flags)
                }
            }
        }.getOrNull()
    }

    private fun PackageManager.getApplicationInfoCompat(
        packageName: String,
        flags: Int = 0,
    ): ApplicationInfo? {
        return runCatching {
            when {
                androidTiramisu() -> {
                    val applicationInfoFlags = PackageManager.ApplicationInfoFlags.of(flags.toLong())
                    getApplicationInfo(packageName, applicationInfoFlags)
                }
                else -> {
                    getApplicationInfo(packageName, flags)
                }
            }
        }.getOrNull()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAboutInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context
        val packageManager = context.packageManager
        val packageName = context.packageName
        val versionName = packageManager.getPackageInfoCompat(packageName, 0)?.versionName ?: unknownVersionName
        binding.version.text = getString(i18n.string.version_s, versionName)
        binding.instant.isVisible =
            view.context.run {
                runCatching {
                    val info = packageManager.getApplicationInfoCompat(packageName, PackageManager.GET_META_DATA)
                    val bundle: Bundle? = info?.metaData
                    val appId = bundle?.getInt(INSTANT_BUILD_FLAVOR, 0) ?: 0
                    appId > 0
                }.getOrNull() ?: false
            }

        binding.musicBy.run {
            val composer = audioManager.getComposerData().firstOrNull()

            if (composer == null) {
                binding.musicCard.isVisible = false
            } else {
                text = getString(i18n.string.music_by, composer.composer)
                setOnClickListener {
                    openComposer(composer.composerLink)
                }
            }
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

    private fun openComposer(composerLink: String) {
        val context = requireContext()
        runCatching {
            analyticsManager.sentEvent(
                Analytics.OpenMusicLink(from = "About"),
            )

            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(composerLink)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(
                context.applicationContext,
                i18n.string.unknown_error,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    companion object {
        private const val INSTANT_BUILD_FLAVOR = "com.google.android.gms.instant.flavor"
    }
}
