package dev.lucasnlm.antimine.preferences

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.XmlRes
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.themes.ThemeActivity
import dev.lucasnlm.antimine.ui.ThematicActivity
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.ext.toInvertedAndroidColor
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.external.IAnalyticsManager
import kotlinx.android.synthetic.main.activity_preferences.*
import org.koin.android.ext.android.inject

class PreferencesActivity :
    ThematicActivity(R.layout.activity_preferences),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val preferenceRepository: IPreferencesRepository by inject()
    private val themeRepository: IThemeRepository by inject()
    private val cloudSaveManager by inject<CloudSaveManager>()

    @XmlRes
    private var currentTabXml: Int = R.xml.gameplay_preferences

    private fun getColorListCompat(): ColorStateList {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked),
        )

        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.colorAccent, typedValue, true)

        val colors = intArrayOf(
            typedValue.data.toAndroidColor(),
            themeRepository.getTheme().palette.background.toInvertedAndroidColor(160)
        )

        return ColorStateList(states, colors)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

        navView.apply {
            setBackgroundColor(themeRepository.getTheme().palette.background)
            val colorList = getColorListCompat()
            itemIconTintList = colorList
            itemTextColor = colorList
            setOnNavigationItemSelectedListener {
                currentTabXml = R.xml.gameplay_preferences
                when (it.itemId) {
                    R.id.gameplay -> placePreferenceFragment(R.xml.gameplay_preferences)
                    R.id.appearance -> placePreferenceFragment(R.xml.appearance_preferences)
                    R.id.general -> placePreferenceFragment(R.xml.general_preferences)
                }
                true
            }
        }

        placePreferenceFragment(R.xml.gameplay_preferences)
    }

    override fun onDestroy() {
        super.onDestroy()

        cloudSaveManager.uploadSave()

        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun placePreferenceFragment(@XmlRes targetPreferences: Int) {
        supportFragmentManager.apply {
            popBackStack()

            findFragmentByTag(PrefsFragment.TAG)?.let { it ->
                beginTransaction().apply {
                    remove(it)
                    commitAllowingStateLoss()
                }
            }

            beginTransaction().apply {
                replace(R.id.preference_fragment, PrefsFragment.newInstance(targetPreferences), PrefsFragment.TAG)
                commitAllowingStateLoss()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (preferenceRepository.hasCustomizations()) {
            menuInflater.inflate(R.menu.delete_icon_menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.delete) {
            if (preferenceRepository.hasCustomizations()) {
                preferenceRepository.reset()
                placePreferenceFragment(currentTabXml)
                invalidateOptionsMenu()
            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    class PrefsFragment : PreferenceFragmentCompat() {
        private val analyticsManager: IAnalyticsManager by inject()

        private var targetPreferences: Int = 0

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // Load the preferences from an XML resource
            targetPreferences = arguments?.getInt(TARGET_PREFS, 0) ?: 0
            if (targetPreferences != 0) {
                addPreferencesFromResource(targetPreferences)
            }

            findPreference<Preference>(SELECT_THEME_PREFS)?.setOnPreferenceClickListener {
                analyticsManager.sentEvent(Analytics.OpenSettings)
                Intent(context, ThemeActivity::class.java).apply {
                    startActivity(this)
                }
                true
            }
        }

        companion object {
            val TAG = PrefsFragment::class.simpleName

            private const val TARGET_PREFS = "target_prefs"
            private const val SELECT_THEME_PREFS = "preference_select_key"

            fun newInstance(targetPreferences: Int): PrefsFragment {
                val args = Bundle().apply {
                    putInt(TARGET_PREFS, targetPreferences)
                }

                return PrefsFragment().apply {
                    arguments = args
                }
            }
        }
    }
}
