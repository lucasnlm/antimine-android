package dev.lucasnlm.antimine.preferences

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint

import dev.lucasnlm.antimine.R

@AndroidEntryPoint
class PreferencesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty)
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
