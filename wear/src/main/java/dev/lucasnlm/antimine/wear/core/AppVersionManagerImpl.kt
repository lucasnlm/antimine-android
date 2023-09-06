package dev.lucasnlm.antimine.wear.core

import dev.lucasnlm.antimine.core.AppVersionManager

class AppVersionManagerImpl : AppVersionManager {
    override fun isValid(): Boolean = true

    override fun isWatch(): Boolean = true
}
