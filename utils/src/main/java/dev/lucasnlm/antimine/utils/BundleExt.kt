package dev.lucasnlm.antimine.utils

import android.os.Bundle
import android.os.Parcelable
import dev.lucasnlm.antimine.utils.BuildExt.androidTiramisu
import java.io.Serializable

object BundleExt {
    inline fun <reified T : Serializable> Bundle.serializable(key: String): T? =
        when {
            androidTiramisu() -> {
                getSerializable(key, T::class.java)
            }
            else -> {
                @Suppress("DEPRECATION")
                getSerializable(key) as? T
            }
        }

    inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? =
        when {
            androidTiramisu() -> {
                getParcelable(key, T::class.java)
            }
            else -> {
                @Suppress("DEPRECATION")
                getParcelable(key) as? T
            }
        }

    inline fun <reified T : Serializable> Bundle.serializableNonSafe(key: String): T {
        return serializable(key)!!
    }
}
