package com.github.zzorgg.beezle.ui.screens.duel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.zzorgg.beezle.data.model.duel.DuelState
import com.github.zzorgg.beezle.data.model.duel.DuelUser
import com.github.zzorgg.beezle.data.repository.DuelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

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

    fun startDuel(username: String) {
        val user = DuelUser(
            id = generateUserId(),
            username = username
        )

        duelRepository.setCurrentUser(user)

        if (!duelState.value.isConnected) {
            connectToServer()
        }

        viewModelScope.launch {
            // Wait a bit for connection if needed
            delay(1000)
            duelRepository.joinQueue(user)
        }
    }

    fun joinQueue() {
        duelRepository.getCurrentUser()?.let { user ->
            duelRepository.joinQueue(user)
        }
    }

    fun leaveQueue() {
        duelRepository.leaveQueue()
    }

    fun submitAnswer(answerIndex: Int) {
        duelRepository.submitAnswer(answerIndex)
    }

    fun clearError() {
        duelRepository.clearError()
    }

    fun clearLastRoundResult() {
        duelRepository.clearLastRoundResult()
    }

    private fun generateUserId(): String {
        return "user_${System.currentTimeMillis()}_${Random.Default.nextInt(1000, 9999)}"
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}