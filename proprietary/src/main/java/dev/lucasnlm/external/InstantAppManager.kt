package dev.lucasnlm.external

import android.content.Context
import com.google.android.gms.instantapps.InstantApps

class InstantAppManager : IInstantAppManager {
    override fun isEnabled(context: Context): Boolean {
        return InstantApps.getPackageManagerCompat(context).isInstantApp
    }
}
