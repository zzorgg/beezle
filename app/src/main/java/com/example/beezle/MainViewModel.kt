package com.example.beezle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beezle.data.local.LocalData
import com.example.beezle.data.local.LocalDataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val localDataStoreRepository: LocalDataStoreRepository
) : ViewModel() {
    private val _isSplashFinished = MutableStateFlow(false)
    val isSplashFinished = _isSplashFinished.asStateFlow()

    val localData: StateFlow<LocalData> = localDataStoreRepository.localData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LocalData()
    )

    init {
        viewModelScope.launch {
            // Simulate a long-running task
            kotlinx.coroutines.delay(3500)
            _isSplashFinished.value = true
        }
    }

    fun finishOnBoarding() {
        viewModelScope.launch(Dispatchers.IO) {
            localDataStoreRepository.update(localData.first().copy(hasOnboarded = true))
        }
    }

    fun connectedWallet() {
        viewModelScope.launch(Dispatchers.IO) {
            localDataStoreRepository.update(localData.first().copy(hasConnectedWallet = true))
        }
    }
}
