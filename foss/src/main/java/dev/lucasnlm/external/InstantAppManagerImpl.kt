package dev.lucasnlm.external

import android.content.Context

class InstantAppManagerImpl : InstantAppManager {
    override fun isEnabled(context: Context): Boolean {
        return false
    }
}
