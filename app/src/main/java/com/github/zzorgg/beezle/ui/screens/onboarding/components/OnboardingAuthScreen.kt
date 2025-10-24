package com.github.zzorgg.beezle.ui.screens.onboarding.components

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.zzorgg.beezle.ui.screens.profile.AuthStatus
import com.github.zzorgg.beezle.ui.screens.profile.NoGoogleAccountScreen
import com.github.zzorgg.beezle.ui.screens.profile.ProfileViewModel
import com.github.zzorgg.beezle.ui.screens.profile.components.AuthPrompt

@Composable
fun OnboardingAuthScreenRoot(
    onSignedIn: () -> Unit,
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val uiState by viewModel.profileViewState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity

    // Navigate to next step when sign-in succeeds
    LaunchedEffect(uiState.firebaseAuthStatus) {
        if (uiState.firebaseAuthStatus is AuthStatus.Success) {
            onSignedIn()
        }
    }

    // If no Google account is found, reuse existing dialog
    if (uiState.firebaseAuthStatus is AuthStatus.NoGoogleAccount) {
        NoGoogleAccountScreen()
        return
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val status = uiState.firebaseAuthStatus) {
                AuthStatus.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                is AuthStatus.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(status.message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        AuthPrompt(signingIn = false) {
                            if (activity != null) viewModel.signin(activity)
                        }
                    }
                }
                AuthStatus.Waiting, AuthStatus.Success, AuthStatus.NoGoogleAccount -> {
                    // Success is handled by LaunchedEffect; show prompt for Waiting
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AuthPrompt(signingIn = status is AuthStatus.Loading) {
                            if (activity != null) viewModel.signin(activity)
                        }
                    }
                }
            }
        }
    }
}
