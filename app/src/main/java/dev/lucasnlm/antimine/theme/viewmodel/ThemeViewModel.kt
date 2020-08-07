package dev.lucasnlm.antimine.theme.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.core.themes.model.AppTheme
import dev.lucasnlm.antimine.core.themes.repository.IThemeRepository

class ThemeViewModel @ViewModelInject constructor(
    private val themeRepository: IThemeRepository
) : ViewModel() {
    val theme = MutableLiveData<AppTheme>()

    fun getThemes(): List<AppTheme> = themeRepository.getAllThemes()

    fun setTheme(appTheme: AppTheme) {
        themeRepository.setTheme(appTheme)
        theme.postValue(appTheme)
    }
}

