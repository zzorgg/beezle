package com.example.beezle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _isSplashFinished = MutableStateFlow(false)
    val isSplashFinished = _isSplashFinished.asStateFlow()

    init {
        viewModelScope.launch {
            // Simulate a long-running task
            kotlinx.coroutines.delay(3500)
            _isSplashFinished.value = true
        }
    }
}
