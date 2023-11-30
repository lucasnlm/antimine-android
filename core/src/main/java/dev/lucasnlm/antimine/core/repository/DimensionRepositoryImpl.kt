package dev.lucasnlm.antimine.core.repository

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.TypedArray
import android.os.Build
import android.util.DisplayMetrics
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.ViewConfiguration
import dev.lucasnlm.antimine.core.R
import kotlin.math.min

class DimensionRepositoryImpl(
    private val context: Context,
) : DimensionRepository {

    private val hasNavBar: Boolean by lazy {
        val resources = context.resources
        val id = resources.getIdentifier(NAVIGATION_BAR_CONFIG, "bool", "android")
        if (id > 0) {
            isEmulator() || resources.getBoolean(id)
        } else {
            val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
            val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
            !hasMenuKey && !hasBackKey
        }
    }

    override fun areaSize(): Float {
        val displayMetrics = displayMetrics()
        val minSide =
            min(
                displayMetrics.widthPixels,
                displayMetrics.heightPixels,
            )
        return minSide / 11.0f
    }

    override fun areaSeparator(): Float {
        return context.resources.getDimension(R.dimen.field_padding)
    }

    override fun areaSizeWithPadding(): Float {
        return areaSize() + 2 * areaSeparator()
    }

    override fun displayMetrics(): DisplayMetrics {
        return Resources.getSystem().displayMetrics
    }

    override fun actionBarSizeWithStatus(): Int {
        val styledAttributes: TypedArray =
            context.theme.obtainStyledAttributes(
                IntArray(1) { android.R.attr.actionBarSize },
            )

        val resourceId: Int = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBar =
            if (resourceId > 0) {
                context.resources.getDimensionPixelSize(resourceId)
            } else {
                0
            }

        val actionBarSize: Int = styledAttributes.getDimension(0, 0.0f).toInt()
        styledAttributes.recycle()

        return actionBarSize + statusBar
    }

    override fun actionBarSize(): Int {
        val styledAttributes: TypedArray =
            context.theme.obtainStyledAttributes(
                IntArray(1) { android.R.attr.actionBarSize },
            )

        val actionBarSize: Int = styledAttributes.getDimension(0, 0.0f).toInt()
        styledAttributes.recycle()

        return actionBarSize
    }

    /**
     * Determines if the system UI navigation bar is located at the bottom.
     * Takes into account the current system orientation and screen size.
     * @return True if navigation bar is located at the bottom of the screen. False otherwise.
     */
    private fun isNavigationBarAtBottom(): Boolean {
        val orientation = context.resources.configuration.orientation

        // On big screens (tablets), the navigation bar will be on bottom in landscape orientation as well.
        // See AOSP `RenderSessionImpl.findNavigationBar` code for reference:
        // https://android.googlesource.com/platform/frameworks/base/+/android-4.2.2_r1/tools/layoutlib/bridge/src/com/android/layoutlib/bridge/impl/RenderSessionImpl.java
        if (orientation == Configuration.ORIENTATION_LANDSCAPE &&
            context.resources.configuration.smallestScreenWidthDp < 600
        ) {
            return false
        }
        return true
    }

    override fun navigationBarHeight(): Int {
        var navHeight = 0
        if (hasNavBar()) {
            val resources = context.resources
            val resourceId: Int =
                resources.getIdentifier(
                    NAVIGATION_BAR_HEIGHT,
                    DEF_TYPE_DIMEN,
                    DEF_PACKAGE,
                )
            if (resourceId > 0) {
                navHeight = resources.getDimensionPixelSize(resourceId)
            }
        }
        return navHeight
    }

    override fun verticalNavigationBarHeight(): Int {
        if (isNavigationBarAtBottom()) {
            return navigationBarHeight()
        }
        return 0
    }

    override fun horizontalNavigationBarHeight(): Int {
        if (!isNavigationBarAtBottom()) {
            return navigationBarHeight()
        }
        return 0
    }

    private fun hasNavBar(): Boolean = hasNavBar

    private fun isEmulator(): Boolean {
        val emulatorBrand =
            listOf(
                Build.BRAND.startsWith("generic"),
                Build.DEVICE.startsWith("generic"),
                Build.FINGERPRINT.startsWith("generic"),
                Build.FINGERPRINT.startsWith("unknown"),
                Build.HARDWARE.contains("goldfish"),
                Build.HARDWARE.contains("ranchu"),
                Build.MANUFACTURER.contains("Genymotion"),
            ).any()

        val emulatorModel =
            listOf(
                "google_sdk",
                "Emulator",
                "Android SDK built for x86",
                "sdk_google",
                "google_sdk",
                "sdk",
                "sdk_x86",
                "vbox86p",
                "emulator",
                "simulator",
            ).any {
                Build.MODEL.contains(it) || Build.PRODUCT.contains(it)
            }

        return emulatorBrand || emulatorModel
    }

    companion object {
        private const val NAVIGATION_BAR_CONFIG = "config_showNavigationBar"
        private const val NAVIGATION_BAR_HEIGHT = "navigation_bar_height"
        private const val DEF_TYPE_DIMEN = "dimen"
        private const val DEF_PACKAGE = "android"
    }
}
