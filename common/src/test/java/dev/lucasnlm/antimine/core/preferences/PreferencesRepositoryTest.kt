package dev.lucasnlm.antimine.core.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class TestPreferenceManager : IPreferencesManager {
    val values = mutableMapOf<String, Any>()

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return values.getOrDefault(key, defaultValue) as Boolean
    }

    override fun putBoolean(key: String, value: Boolean) {
        values[key] = value
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return values.getOrDefault(key, defaultValue) as Int
    }

    override fun putInt(key: String, value: Int) {
        values[key] = value
    }

    override fun removeKey(key: String) {
        values.remove(key)
    }

    override fun contains(key: String): Boolean {
        return values.containsKey(key)
    }
}

class PreferencesRepositoryTest {
    @Test
    fun testMigrationWhenUsingDoubleClick() {
        val preferenceManager = TestPreferenceManager()
        preferenceManager.putBoolean("preference_double_click_open", true)
        assertTrue(preferenceManager.values["preference_double_click_open"] as Boolean)

        val preferencesRepository = PreferencesRepository(preferenceManager)

        assertTrue(preferenceManager.values["preference_double_click_open"] == null)
        assertEquals(1, preferenceManager.values["preference_control_style"])
        assertFalse(preferencesRepository.getBoolean("preference_double_click_open", false))
    }

    @Test
    fun testMigrationWhenNotUsingDoubleClick() {
        val preferenceManager = TestPreferenceManager()
        preferenceManager.putBoolean("preference_double_click_open", false)
        assertFalse(preferenceManager.values["preference_double_click_open"] as Boolean)

        val preferencesRepository = PreferencesRepository(preferenceManager)

        assertTrue(preferenceManager.values["preference_double_click_open"] == null)
        assertFalse(preferencesRepository.getBoolean("preference_double_click_open", false))
    }

    @Test
    fun testMigrationLargeArea() {
        val preferenceManager = TestPreferenceManager()
        preferenceManager.putBoolean("preference_large_area", true)
        assertTrue(preferenceManager.values["preference_large_area"] as Boolean)

        val preferencesRepository = PreferencesRepository(preferenceManager)

        assertTrue(preferenceManager.values["preference_large_area"] == null)
        assertEquals(63, preferencesRepository.getInt("preference_area_size", -1))
    }

    @Test
    fun testMigrationLargeAreaOff() {
        val preferenceManager = TestPreferenceManager()
        val preferencesRepository = PreferencesRepository(preferenceManager)

        assertTrue(preferenceManager.values["preference_large_area"] == null)
        assertEquals(50, preferencesRepository.getInt("preference_area_size", 50))
    }
}
