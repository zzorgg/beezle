package com.github.zzorgg.beezle.ui.screens.onboarding.components

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.zzorgg.beezle.R
import com.github.zzorgg.beezle.ui.screens.profile.AuthStatus
import com.github.zzorgg.beezle.ui.screens.profile.NoGoogleAccountScreen
import com.github.zzorgg.beezle.ui.screens.profile.ProfileViewModel
import com.github.zzorgg.beezle.ui.screens.profile.ProfileViewState
import com.github.zzorgg.beezle.ui.theme.BeezleTheme

@Composable
fun OnboardingAuthScreenRoot(
    onSignedIn: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val viewModel: ProfileViewModel = hiltViewModel()
    val uiState by viewModel.profileViewState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.firebaseAuthStatus) {
        if (uiState.firebaseAuthStatus is AuthStatus.Success) {
            onSignedIn()
        }
    }

    if (uiState.firebaseAuthStatus is AuthStatus.NoGoogleAccount) {
        NoGoogleAccountScreen()
        return
    }

    OnboardingAuthScreen(
        uiState = uiState,
        signInCallback = { if (activity != null) viewModel.signin(activity) }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun OnboardingAuthScreen(
    uiState: ProfileViewState,
    signInCallback: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(1.25f)
                    .padding(bottom = 120.dp)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center,
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.onboarding_background),
                        contentDescription = null,
                        modifier = Modifier
                            .scale(3f)
                            .offset(x = (-50).dp)
                    )
                }
                Column(modifier = Modifier.offset(x = 70.dp)) {
                    Text(
                        text = "Beezle",
                        style = MaterialTheme.typography.displayLargeEmphasized,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "App",
                            style = MaterialTheme.typography.displayLargeEmphasized,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.mipmap.main_icon_background),
                                contentDescription = "App icon",
                            )
                            Image(
                                painter = painterResource(id = R.mipmap.main_icon_foreground),
                                contentDescription = "App icon",
                                modifier = Modifier.scale(1.5f),
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "Saif Ali Khan".uppercase(),
                            style = MaterialTheme.typography.bodyMediumEmphasized
                        )
                        Text(
                            text = "Shoaib Khan".uppercase(),
                            style = MaterialTheme.typography.bodyMediumEmphasized
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
                    .align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val status = uiState.firebaseAuthStatus
                if (status is AuthStatus.Error) {
                    Text(status.message, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = signInCallback,
                    enabled = status !is AuthStatus.Loading,
                    modifier = Modifier.padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                ) {
                    if (status is AuthStatus.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.android_neutral_rd_na_no_bg),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(if (status is AuthStatus.Loading) "Signing in..." else "Sign in with Google")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingAuthScreenRootPreview() {
    BeezleTheme {
        OnboardingAuthScreen(
            uiState = ProfileViewState(),
            signInCallback = {},
        )
    }
}
