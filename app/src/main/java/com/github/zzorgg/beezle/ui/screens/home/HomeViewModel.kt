package com.github.zzorgg.beezle.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.zzorgg.beezle.data.local.LocalData
import com.github.zzorgg.beezle.data.local.LocalDataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val localDataStoreRepository: LocalDataStoreRepository
) : ViewModel() {
    val localData: StateFlow<LocalData> = localDataStoreRepository.localData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LocalData()
    )

    fun finishWelcomeGif() {
        viewModelScope.launch(Dispatchers.IO) {
            localDataStoreRepository.update(localData.first().copy(hasWelcomeGifCompleted = true))
        }
    }
}
