package com.github.zzorgg.beezle.ui.screens.duel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.zzorgg.beezle.data.model.duel.DuelMode
import com.github.zzorgg.beezle.data.model.duel.DuelState
import com.github.zzorgg.beezle.data.repository.DuelRepository
import com.github.zzorgg.beezle.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DuelViewModel @Inject constructor(
    private val duelRepository: DuelRepository,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {
    val duelState: StateFlow<DuelState> = duelRepository.duelState

    init {
        viewModelScope.launch { duelRepository.observeConnectionStatus() }
        viewModelScope.launch { duelRepository.observeWebSocketMessages() }
    }

    fun getMyId() = userProfileRepository.getCurrentUid()

    fun connectToServer() {
        duelRepository.connectToServer()
    }

    fun disconnect() {
        duelRepository.disconnect()
    }

    fun startDuel(username: String, mode: DuelMode) {
        viewModelScope.launch {
            duelRepository.startDuel(username, mode)
        }
    }

    fun leaveQueue() {
        viewModelScope.launch {
            duelRepository.leaveQueue()
        }
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
}