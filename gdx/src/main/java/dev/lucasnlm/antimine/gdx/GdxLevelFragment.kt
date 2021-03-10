package dev.lucasnlm.antimine.gdx

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication

open class GdxLevelFragment : AndroidFragmentApplication() {
    private val levelApplicationListener = LevelApplicationListener()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val config = AndroidApplicationConfiguration().apply {
            numSamples = 2
            useAccelerometer = false
            useCompass = false
            useGyroscope = false
            useWakelock = false
        }
        return initializeForView(levelApplicationListener, config)
    }

    companion object {
        val TAG = GdxLevelFragment::class.simpleName
    }
}
