package com.example.beezle.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beezle.data.model.profile.UserProfile
import com.example.beezle.data.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProfileUiState {
    object Idle: ProfileUiState
    object Loading: ProfileUiState
    object SignedOut: ProfileUiState
    data class Loaded(val profile: UserProfile): ProfileUiState
    data class Error(val message: String): ProfileUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: UserProfileRepository,
    private val auth: FirebaseAuth
): ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var isLoading = false

    fun refresh(walletPublicKey: String? = null) {
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = ProfileUiState.SignedOut
            return
        }
        if (isLoading) return
        _uiState.value = ProfileUiState.Loading
        isLoading = true
        viewModelScope.launch {
            try {
                var profile = repo.getProfile(user.uid)
                if (profile == null) {
                    profile = repo.createProfile(user.uid, user.displayName ?: user.email?.substringBefore('@'))
                }
                // link wallet if provided and not already linked
                if (!walletPublicKey.isNullOrBlank() && profile.walletPublicKey == null) {
                    repo.linkWallet(user.uid, walletPublicKey)
                    profile = profile.copy(walletPublicKey = walletPublicKey)
                }
                _uiState.value = ProfileUiState.Loaded(profile)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.localizedMessage ?: "Failed to load profile")
            } finally {
                isLoading = false
            }
        }
    }

    fun setUsername(username: String) {
        val current = (_uiState.value as? ProfileUiState.Loaded)?.profile ?: return
        if (username.isBlank()) return
        viewModelScope.launch {
            try {
                repo.updateUsername(current.uid, username)
                _uiState.value = ProfileUiState.Loaded(current.copy(username = username))
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.localizedMessage ?: "Failed to update username")
            }
        }
    }

    fun linkWallet(walletPublicKey: String) {
        val current = (_uiState.value as? ProfileUiState.Loaded)?.profile ?: return
        if (current.walletPublicKey == walletPublicKey) return
        viewModelScope.launch {
            try {
                repo.linkWallet(current.uid, walletPublicKey)
                _uiState.value = ProfileUiState.Loaded(current.copy(walletPublicKey = walletPublicKey))
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.localizedMessage ?: "Failed to link wallet")
            }
        }
    }
}
