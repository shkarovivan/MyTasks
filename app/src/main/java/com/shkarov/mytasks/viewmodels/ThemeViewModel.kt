package com.shkarov.mytasks.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shkarov.mytasks.settings.ThemeSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeSettings: ThemeSettings
) : ViewModel() {

    val darkThemeEnabled = themeSettings.darkThemeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setDarkThemeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themeSettings.setDarkThemeEnabled(enabled)
        }
    }
}