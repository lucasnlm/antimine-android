package dev.lucasnlm.antimine.preferences

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

import dev.lucasnlm.antimine.R

class PreferencesActivity : AppCompatActivity(R.layout.activity_empty) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        // Load the preferences from an XML resource
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content, PrefsFragment())
            .commitAllowingStateLoss()
    }

    class PrefsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences)
        }
    }
}
