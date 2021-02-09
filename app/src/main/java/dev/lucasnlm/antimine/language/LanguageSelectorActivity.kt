package dev.lucasnlm.antimine.language

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.PreferencesActivity
import dev.lucasnlm.antimine.ui.ThematicActivity
import org.koin.android.ext.android.inject
import java.util.Locale

class LanguageSelectorActivity : ThematicActivity(R.layout.activity_language) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        placePreferenceFragment()
    }

    private fun placePreferenceFragment() {
        supportFragmentManager.apply {
            popBackStack()

            findFragmentByTag(PreferencesActivity.PrefsFragment.TAG)?.let { it ->
                beginTransaction().apply {
                    remove(it)
                    commitAllowingStateLoss()
                }
            }

            beginTransaction().apply {
                replace(
                    R.id.preference_fragment,
                    LanguageListFragment(),
                    LanguageListFragment.TAG,
                )
                commitAllowingStateLoss()
            }
        }
    }

    class LanguageListFragment : PreferenceFragmentCompat() {
        private val preferenceRepository: IPreferencesRepository by inject()

        private val languagesMap = mapOf(
            "Afrikaans" to "af-rZA",
            "العربية" to "ar-rSA",
            "Català" to "ca-rES",
            "Čeština" to "cs-rCZ",
            "Dansk" to "da-rDK",
            "Deutsch" to "de-rDE",
            "ελληνικά" to "el-rGR",
            "English" to "en-rUS",
            "Español" to "es-rES",
            "Suomi" to "fi-rFI",
            "Français" to "fr-rFR",
            "हिन्दी" to "hi-rIN",
            "Magyar" to "hu-rHU",
            "Italiano" to "it-rIT",
            "תירבע" to "iw-rIL",
            "日本語" to "ja-rJP",
            "한국어" to "ko-rKR",
            "Nederlands" to "nl-rNL",
            "Bokmål" to "no-rNO",
            "Polski" to "pl-rPL",
            "Português (BR)" to "pt-rBR",
            "Português (PT)" to "pt-rPT",
            "Română" to "ro-rRO",
            "Pусский" to "ru-rRU",
            "Svenska" to "sv-rSE",
            "ไทย" to "th-rTH",
            "Türkçe" to "tr-rTR",
            "Yкраїньска" to "uk-rUA",
            "Tiểng Việt" to "vi-rVN",
            "中文" to "zh-rCN",
            "български" to "bg-rBG",
            "Bahasa Indonesia" to "in-rID",
            "Vèneto" to "vec-rIT",
            "زمانی کوردی" to "ku-rTR",
        )

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.language_preferences)

            findPreference<PreferenceCategory>(LANGUAGE_LIST)?.apply {
                languagesMap
                    .mapValues {
                        val language = it.value.split("-")
                        Locale(language.first(), language.last())
                    }
                    .toList()
                    .sortedBy {
                        it.first
                    }
                    .forEach { (language, locale) ->
                        addPreference(
                            Preference(context).apply {
                                title = language
                                isIconSpaceReserved = false
                                setOnPreferenceClickListener {
                                    preferenceRepository.setPreferredLocale("${locale.language}-${locale.country}")
                                    activity?.finish()
                                    true
                                }
                            }
                        )
                    }
            }
        }

        companion object {
            val TAG = LanguageListFragment::class.simpleName
            private const val LANGUAGE_LIST = "language_list"
        }
    }
}
