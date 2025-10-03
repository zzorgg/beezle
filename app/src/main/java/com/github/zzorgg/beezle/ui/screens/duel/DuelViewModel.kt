package com.github.zzorgg.beezle.ui.screens.duel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.zzorgg.beezle.data.model.duel.DuelState
import com.github.zzorgg.beezle.data.model.duel.DuelUser
import com.github.zzorgg.beezle.data.repository.AuthRepository
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
    private val authRepository: AuthRepository,
) : ViewModel() {

    val duelState: StateFlow<DuelState> = duelRepository.duelState

    fun connectToServer() {
        duelRepository.connectToServer()
    }

    fun disconnect() {
        duelRepository.disconnect()
    }

    fun startDuel() {
        viewModelScope.launch {
            authRepository.currentUser()?.let { firebaseUser ->

                val user = DuelUser(
                    id = firebaseUser.uid,
                    username = firebaseUser.displayName ?: "No name",
                    avatarUrl = firebaseUser.photoUrl.toString()
                )

                duelRepository.setCurrentUser(user)

                if (!duelState.value.isConnected) {
                    connectToServer()
                }

                // Wait a bit for connection if needed
                delay(1000)
                duelRepository.joinQueue(user)
            }
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

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}