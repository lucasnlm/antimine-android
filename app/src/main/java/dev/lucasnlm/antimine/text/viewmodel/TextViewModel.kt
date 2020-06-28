package dev.lucasnlm.antimine.text.viewmodel

import android.app.Application
import androidx.annotation.RawRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class TextViewModel(
    application: Application
) : AndroidViewModel(application) {

    val text = MutableLiveData<String>()

    fun loadText(@RawRes rawFile: Int) {
        val result = getApplication<Application>().resources.openRawResource(rawFile)
            .bufferedReader()
            .readLines()
            .joinToString("\n")

        text.postValue(result)
    }
}
