package dev.lucasnlm.antimine.utils

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

object BuildExt {
    /**
     * Executes the block if the current Android version
     * is at least [Build.VERSION_CODES.O].
     *
     * @return true if the block was executed, false otherwise.
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
    fun androidOreo(block: (() -> Unit)? = null): Boolean {
        return withAndroidVersion(Build.VERSION_CODES.O, block)
    }

    /**
     * Executes the block if the current Android version
     * is at least [Build.VERSION_CODES.TIRAMISU].
     *
     * @return true if the block was executed, false otherwise.
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
    fun androidTiramisu(block: (() -> Unit)? = null): Boolean {
        return withAndroidVersion(Build.VERSION_CODES.TIRAMISU, block)
    }

    /**
     * Executes the block if the current Android version
     * is at least [Build.VERSION_CODES.M].
     *
     * @return true if the block was executed, false otherwise.
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
    fun androidMarshmallow(block: (() -> Unit)? = null): Boolean {
        return withAndroidVersion(Build.VERSION_CODES.M, block)
    }

    /**
     * Executes the block if the current Android version
     * is at least [Build.VERSION_CODES.N].
     *
     * @return true if the block was executed, false otherwise.
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
    fun androidNougat(block: (() -> Unit)? = null): Boolean {
        return withAndroidVersion(Build.VERSION_CODES.N, block)
    }

    /**
     * Executes the block if the current Android version
     * is at least [Build.VERSION_CODES.S].
     *
     * @return true if the block was executed, false otherwise.
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
    fun androidSnowCone(block: (() -> Unit)? = null): Boolean {
        return withAndroidVersion(Build.VERSION_CODES.S, block)
    }

    /**
     * Executes the block if the current Android version
     * is at least [Build.VERSION_CODES.UPSIDE_DOWN_CAKE].
     *
     * @return true if the block was executed, false otherwise.
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun androidUpsideDownCake(block: (() -> Unit)? = null): Boolean {
        return withAndroidVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE, block)
    }

    /**
     * Executes the block if the current Android version
     * is at least [Build.VERSION_CODES.Q].
     *
     * @return true if the block was executed, false otherwise.
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
    fun androidQuinceTart(block: (() -> Unit)? = null): Boolean {
        return withAndroidVersion(Build.VERSION_CODES.Q, block)
    }

    @SuppressLint("AnnotateVersionCheck")
    private fun withAndroidVersion(
        version: Int,
        block: (() -> Unit)? = null,
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= version) {
            block?.invoke()
            true
        } else {
            false
        }
    }
}
