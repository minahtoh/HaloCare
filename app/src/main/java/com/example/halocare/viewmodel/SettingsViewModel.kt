package com.example.halocare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val isDarkMode = settingsRepository.darkModeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    fun toggleDarkMode() {
        viewModelScope.launch {
            settingsRepository.setDarkMode(!isDarkMode.value)
        }
    }
}
