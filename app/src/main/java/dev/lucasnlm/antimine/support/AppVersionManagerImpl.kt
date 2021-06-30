package dev.lucasnlm.antimine.support

import dev.lucasnlm.antimine.core.IAppVersionManager

class AppVersionManagerImpl(
    private val debug: Boolean,
    private val id: String,
) : IAppVersionManager {
    private val valid by lazy {
        debug || id == "com.logical.minato" || id == "dev.lucasnlm.antimine" || id == "dev.lucanlm.antimine"
    }

    override fun isValid(): Boolean {
        // If you want to distribution a fork of this game,
        // please check its LICENSE and conditions before do it.
        // Any fork / changed version must also be open sourced.
        return valid
    }
}
