package dev.lucasnlm.antimine.l10n

import java.util.*

interface GameLocaleManager {
    fun getAllGameLocaleTags(): List<String>

    fun setGameLocale(tag: String)

    fun getGameLocale(): Locale?

    fun applyPreferredLocaleIfNeeded()
}
