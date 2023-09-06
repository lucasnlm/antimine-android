package dev.lucasnlm.antimine.core

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

inline fun <reified T : Serializable> Bundle.serializable(key: String): T? =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            getSerializable(key, T::class.java)
        }
        else -> {
            @Suppress("DEPRECATION")
            getSerializable(key) as? T
        }
    }

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
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
