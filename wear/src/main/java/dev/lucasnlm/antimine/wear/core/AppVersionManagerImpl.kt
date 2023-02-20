package dev.lucasnlm.antimine.wear.core

import dev.lucasnlm.antimine.core.IAppVersionManager

class AppVersionManagerImpl : IAppVersionManager {
    override fun isValid(): Boolean = true
    override fun isWatch(): Boolean = true
}
