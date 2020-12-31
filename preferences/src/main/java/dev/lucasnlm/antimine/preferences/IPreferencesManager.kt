package dev.lucasnlm.antimine.preferences

interface IPreferencesManager {
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun getInt(key: String, defaultValue: Int): Int
    fun putInt(key: String, value: Int)
    fun removeKey(key: String)
    fun contains(key: String): Boolean
}
