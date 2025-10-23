package com.github.zzorgg.beezle.ui.screens.duel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.zzorgg.beezle.data.model.duel.DuelMode
import com.github.zzorgg.beezle.data.model.duel.DuelState
import com.github.zzorgg.beezle.data.repository.DuelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DuelViewModel @Inject constructor(
    private val duelRepository: DuelRepository
) : ViewModel() {

    val duelState: StateFlow<DuelState> = duelRepository.duelState

    fun connectToServer() {
        duelRepository.connectToServer()
    }

    fun disconnect() {
        duelRepository.disconnect()
    }

    fun startDuel(username: String, mode: DuelMode) {
        duelRepository.startDuel(username, mode)
    }

    fun leaveQueue() {
        duelRepository.leaveQueue()
    }

    fun submitAnswer(answer: String) {
        duelRepository.submitAnswer(answer)
    }

    fun clearError() {
        viewModelScope.launch {
            delay(100)
            duelRepository.clearError()
        }
    }

    fun clearGameResult() {
        duelRepository.clearGameResult()
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}