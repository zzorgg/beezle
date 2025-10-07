package com.github.zzorgg.beezle.ui.screens.profile

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.zzorgg.beezle.data.model.profile.UserProfile
import com.github.zzorgg.beezle.data.repository.AuthRepository
import com.github.zzorgg.beezle.data.repository.NoGoogleAccountFoundException
import com.github.zzorgg.beezle.data.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthStatus {
    object Loading : AuthStatus
    object Success : AuthStatus
    object Waiting : AuthStatus
    object NoGoogleAccount : AuthStatus
    data class Error(val message: String) : AuthStatus
}

data class ProfileViewState(
    val firebaseAuthStatus: AuthStatus = AuthStatus.Waiting,
    val userProfileStatus: AuthStatus = AuthStatus.Waiting,
)

data class ProfileDataState(
    val userProfile: UserProfile? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _profileDataState = MutableStateFlow(ProfileDataState())
    val profileDataState: StateFlow<ProfileDataState> = _profileDataState

    private val _profileViewState = MutableStateFlow(ProfileViewState())
    val profileViewState: StateFlow<ProfileViewState> = _profileViewState


    private var isLoading = false

    init {
        viewModelScope.launch {
            val user = authRepository.currentUser()
            if (user == null) return@launch
            _profileViewState.update { it.copy(firebaseAuthStatus = AuthStatus.Success) }
        }
    }

    suspend fun refresh(walletPublicKey: String? = null) {
        if (authRepository.currentUser() == null) return
        if (isLoading) return
        _profileViewState.update { it.copy(userProfileStatus = AuthStatus.Loading) }
        isLoading = true

        try {
            val user = authRepository.currentUser()!!
            var profile = userProfileRepository.getProfile(user.uid)
            if (profile == null) {
                profile = userProfileRepository.createProfile(
                    user.uid,
                    user.displayName ?: user.email?.substringBefore('@')
                )
            }
            // link wallet if provided and not already linked
            if (!walletPublicKey.isNullOrBlank() && profile.walletPublicKey == null) {
                userProfileRepository.linkWallet(user.uid, walletPublicKey)
                profile = profile.copy(walletPublicKey = walletPublicKey)
            }
            _profileDataState.update { it.copy(userProfile = profile) }
            _profileViewState.update { it.copy(userProfileStatus = AuthStatus.Success) }
        } catch (_: Exception) {
            _profileViewState.update { it.copy(userProfileStatus = AuthStatus.Error("Failed to load profile")) }
        } finally {
            isLoading = false
        }
    }

    fun setUsername(username: String) {
        val current = profileDataState.value.userProfile ?: return
        if (username.isBlank()) return
        viewModelScope.launch {
            try {
                userProfileRepository.updateUsername(current.uid, username)
                _profileDataState.update { it.copy(userProfile = current.copy(username = username)) }
            } catch (_: Exception) {
                _profileViewState.update { it.copy(userProfileStatus = AuthStatus.Error("Failed to load profile")) }
            }
        }
    }

    fun linkWallet(walletPublicKey: String) {
        val current = profileDataState.value.userProfile ?: return
        if (current.walletPublicKey == walletPublicKey) return
        viewModelScope.launch {
            try {
                userProfileRepository.linkWallet(current.uid, walletPublicKey)
                _profileDataState.update { it.copy(userProfile = current.copy(walletPublicKey = walletPublicKey)) }
            } catch (_: Exception) {
                _profileViewState.update {
                    it.copy(userProfileStatus = AuthStatus.Error("Failed to load profile"))
                }
            }
        }
    }

    fun acknowledgeNoGoogleAccountNotice() {
        _profileViewState.update { it.copy(firebaseAuthStatus = AuthStatus.Waiting) }
    }

    fun signin(activity: Activity) {
        viewModelScope.launch {
            _profileViewState.update { it.copy(firebaseAuthStatus = AuthStatus.Loading) }
            try {
                val user = authRepository.signin(activity)
                if (user == null) {
                    _profileViewState.update { it.copy(firebaseAuthStatus = AuthStatus.Error("Unable to sign-in")) }
                    return@launch
                }
                _profileViewState.update { it.copy(firebaseAuthStatus = AuthStatus.Success) }
            } catch (e: NoGoogleAccountFoundException) {
                _profileViewState.update { it.copy(firebaseAuthStatus = AuthStatus.NoGoogleAccount) }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Sign in error", e)
                _profileViewState.update { it.copy(firebaseAuthStatus = AuthStatus.Error("Sign in failed: ${e.localizedMessage}")) }
            }
        }
    }

    fun signout() {
        viewModelScope.launch {
            authRepository.signout()
            _profileDataState.update { ProfileDataState() }
            _profileViewState.update { ProfileViewState() }
        }
    }
}
