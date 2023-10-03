package dev.lucasnlm.antimine.wear.core

import dev.lucasnlm.antimine.core.AppVersionManager

/**
 * Information about the app version.
 * Wear implementation doesn't have any restriction.
 */
class AppVersionManagerImpl : AppVersionManager {
    override fun isValid(): Boolean = true

    override fun isWatch(): Boolean = true
}
