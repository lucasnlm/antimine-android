package dev.lucasnlm.antimine.splash.viewmodel

import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.support.IapHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SplashViewModel(
    private val scope: CoroutineScope,
    private val iapHandler: IapHandler,
) : ViewModel() {
    fun startIap() {
        scope.launch {
            iapHandler.start()
        }
    }

    companion object {
        val TAG = SplashViewModel::class.simpleName
    }
}
