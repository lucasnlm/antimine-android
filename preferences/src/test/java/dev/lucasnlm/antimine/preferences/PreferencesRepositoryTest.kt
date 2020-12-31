package dev.lucasnlm.antimine.preferences

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
    fun testProgressValue() {
        val preferenceManager = TestPreferenceManager()
        val preferencesRepository = PreferencesRepository(preferenceManager, 400)

        assertEquals(0, preferencesRepository.getProgressiveValue())

        preferencesRepository.incrementProgressiveValue()
        assertEquals(1, preferencesRepository.getProgressiveValue())

        preferencesRepository.incrementProgressiveValue()
        assertEquals(2, preferencesRepository.getProgressiveValue())

        preferencesRepository.decrementProgressiveValue()
        assertEquals(1, preferencesRepository.getProgressiveValue())

        preferencesRepository.decrementProgressiveValue()
        assertEquals(0, preferencesRepository.getProgressiveValue())

        preferencesRepository.decrementProgressiveValue()
        assertEquals(0, preferencesRepository.getProgressiveValue())
    }

    @Test
    fun testMigrationWhenUsingDoubleClick() {
        val preferenceManager = TestPreferenceManager()
        preferenceManager.putBoolean("preference_double_click_open", true)
        assertTrue(preferenceManager.values["preference_double_click_open"] as Boolean)

        PreferencesRepository(preferenceManager, 400)

        assertTrue(preferenceManager.values["preference_double_click_open"] == null)
        assertEquals(1, preferenceManager.values["preference_control_style"])
        assertFalse(preferenceManager.getBoolean("preference_double_click_open", false))
    }

    @Test
    fun testMigrationWhenNotUsingDoubleClick() {
        val preferenceManager = TestPreferenceManager()
        preferenceManager.putBoolean("preference_double_click_open", false)
        assertFalse(preferenceManager.values["preference_double_click_open"] as Boolean)

        PreferencesRepository(preferenceManager, 400)

        assertTrue(preferenceManager.values["preference_double_click_open"] == null)
        assertFalse(preferenceManager.getBoolean("preference_double_click_open", false))
    }

    @Test
    fun testMigrationLargeAreaOn() {
        val preferenceManager = TestPreferenceManager()
        preferenceManager.putBoolean("preference_large_area", true)
        assertTrue(preferenceManager.values["preference_large_area"] as Boolean)

        PreferencesRepository(preferenceManager, 400)

        assertTrue(preferenceManager.values["preference_large_area"] == null)
        assertEquals(63, preferenceManager.getInt("preference_area_size", -1))
    }

    @Test
    fun testMigrationLargeAreaOff() {
        val preferenceManager = TestPreferenceManager()
        PreferencesRepository(preferenceManager, 400)

        assertTrue(preferenceManager.values["preference_large_area"] == null)
        assertEquals(50, preferenceManager.getInt("preference_area_size", -1))
    }

    @Test
    fun testMigrationLargeAreaFalse() {
        val preferenceManager = TestPreferenceManager()
        preferenceManager.putBoolean("preference_large_area", false)
        assertEquals(false, preferenceManager.values["preference_large_area"] as Boolean)

        dev.lucasnlm.antimine.preferences.PreferencesRepository(preferenceManager, 400)

        assertTrue(preferenceManager.values["preference_large_area"] == null)
        assertEquals(50, preferenceManager.getInt("preference_area_size", -1))
    }
}
