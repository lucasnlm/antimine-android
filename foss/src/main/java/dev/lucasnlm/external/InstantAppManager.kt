package dev.lucasnlm.external

import android.content.Context

class InstantAppManager : IInstantAppManager {
    override fun isEnabled(context: Context): Boolean {
        return false
    }
}
