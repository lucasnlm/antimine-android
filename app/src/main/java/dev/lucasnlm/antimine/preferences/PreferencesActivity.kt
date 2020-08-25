package dev.lucasnlm.antimine.preferences

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.ThematicActivity
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import org.koin.android.ext.android.inject

class PreferencesActivity : ThematicActivity(R.layout.activity_empty) {
    private val preferenceRepository: IPreferencesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        placePreferenceFragment()
    }

    private fun placePreferenceFragment() {
        supportFragmentManager.apply {
            popBackStack()

            findFragmentByTag(PrefsFragment.TAG)?.let { it ->
                beginTransaction().apply {
                    remove(it)
                    commitAllowingStateLoss()
                }
            }

            beginTransaction().apply {
                replace(R.id.content, PrefsFragment(), PrefsFragment.TAG)
                commitAllowingStateLoss()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.delete_icon_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.delete) {
            preferenceRepository.reset()
            placePreferenceFragment()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    class PrefsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences)
        }

        companion object {
            val TAG = PrefsFragment::class.simpleName
        }
    }
}
