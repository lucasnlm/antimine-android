package dev.lucasnlm.external

import android.content.Context

interface IInstantAppManager {
    fun isEnabled(context: Context): Boolean
}
