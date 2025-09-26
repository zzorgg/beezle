package com.example.beezle.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    object Idle: ProfileUiState
    object Loading: ProfileUiState
    data class Loaded(val profile: UserProfile): ProfileUiState
    data class Error(val message: String): ProfileUiState
}

class ProfileViewModel(application: Application): AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadOrCreate(walletPublicKey: String) {
        if (walletPublicKey.isBlank()) return
        _uiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            try {
                val profile = loadProfile(walletPublicKey)
                _uiState.value = ProfileUiState.Loaded(profile)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.localizedMessage ?: "Failed to load profile")
            }
        }
    }

    private fun loadProfile(walletPublicKey: String): UserProfile {
        val base = "profile_${walletPublicKey}" // no encryption, placeholder only
        val username = prefs.getString("${base}_username", null)
        val createdAt = prefs.getLong("${base}_createdAt", System.currentTimeMillis()).also {
            if (!prefs.contains("${base}_createdAt")) prefs.edit().putLong("${base}_createdAt", it).apply()
        }
        val mathLevel = prefs.getInt("${base}_mathLevel", 1)
        val csLevel = prefs.getInt("${base}_csLevel", 1)
        val wins = prefs.getInt("${base}_wins", 0)
        val losses = prefs.getInt("${base}_losses", 0)
        return UserProfile(
            walletPublicKey = walletPublicKey,
            username = username,
            createdAt = createdAt,
            mathLevel = mathLevel,
            csLevel = csLevel,
            duelStats = DuelStats(wins, losses)
        )
    }

    private fun persist(profile: UserProfile) {
        val base = "profile_${profile.walletPublicKey}"
        prefs.edit().apply {
            putString("${base}_username", profile.username)
            putLong("${base}_createdAt", profile.createdAt)
            putInt("${base}_mathLevel", profile.mathLevel)
            putInt("${base}_csLevel", profile.csLevel)
            putInt("${base}_wins", profile.duelStats.wins)
            putInt("${base}_losses", profile.duelStats.losses)
        }.apply()
    }

    fun setUsername(walletPublicKey: String, username: String) {
        val current = (_uiState.value as? ProfileUiState.Loaded)?.profile ?: return
        val updated = current.copy(username = username)
        persist(updated)
        _uiState.value = ProfileUiState.Loaded(updated)
    }
}
