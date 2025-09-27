package com.example.beezle.duel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beezle.duel.data.DuelState
import com.example.beezle.duel.data.DuelUser
import com.example.beezle.duel.repository.DuelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun startDuel(username: String) {
        val user = DuelUser(
            id = generateUserId(),
            username = username,
            avatar = null,
            score = 0
        )

        duelRepository.setCurrentUser(user)

        if (!duelState.value.isConnected) {
            connectToServer()
        }

        viewModelScope.launch {
            // Wait a bit for connection if needed
            kotlinx.coroutines.delay(1000)
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
        return "user_${System.currentTimeMillis()}_${kotlin.random.Random.nextInt(1000, 9999)}"
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
