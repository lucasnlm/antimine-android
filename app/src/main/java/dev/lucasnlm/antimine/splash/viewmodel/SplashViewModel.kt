package dev.lucasnlm.antimine.splash.viewmodel

import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.support.IapHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SplashViewModel(
    private val iapHandler: IapHandler,
) : ViewModel() {
    fun startIap() {
        GlobalScope.launch {
            iapHandler.start()
        }
    }

    companion object {
        val TAG = SplashViewModel::class.simpleName
    }
}
