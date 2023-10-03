package dev.lucasnlm.antimine.core

/**
 * Information about the app version.
 */
interface AppVersionManager {
    /**
     * Check for valid smartphone version.
     */
    fun isValid(): Boolean

    /**
     * Check for Wear OS version.
     */
    fun isWatch(): Boolean
}
