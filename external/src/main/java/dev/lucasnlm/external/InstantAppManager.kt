package dev.lucasnlm.external

import android.content.Context

interface InstantAppManager {
    fun isEnabled(context: Context): Boolean
}
