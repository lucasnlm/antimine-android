package dev.lucasnlm.antimine.common.auto

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import dev.lucasnlm.antimine.utils.BuildExt.androidMarshmallow

object AutoExt {
    fun Context.isAndroidAuto(): Boolean {
        return androidMarshmallow() && featureAutomotive()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun Context.featureAutomotive(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)
    }
}
