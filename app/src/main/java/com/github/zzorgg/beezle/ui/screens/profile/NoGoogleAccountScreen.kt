package com.github.zzorgg.beezle.ui.screens.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.zzorgg.beezle.R
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun NoGoogleAccountScreen() {
    val context = LocalContext.current
    // Load from res/raw to avoid asset path/case issues
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Sign.json.json"))
    val viewModel: ProfileViewModel = hiltViewModel()

    AlertDialog(
        onDismissRequest = { viewModel.acknowledgeNoGoogleAccountNotice() },
        title = { Text("Sign in with Google") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(180.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Google account found on this device. Add a Google account in Settings and try again.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Try to open Add Account screen filtered to Google, with safe fallbacks
                    val intents = listOf(
                        Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                            putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
                        },
                        Intent(Settings.ACTION_SYNC_SETTINGS),
                        Intent(Settings.ACTION_SETTINGS)
                    )
                    for (intent in intents) {
                        try {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                            break
                        } catch (_: ActivityNotFoundException) {
                        } catch (_: Exception) {
                        }
                    }
                    // Dismiss after launching Settings
                    viewModel.acknowledgeNoGoogleAccountNotice()
                }
            ) { Text("Open Settings") }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.acknowledgeNoGoogleAccountNotice() }) {
                Text("Not now")
            }
        }
    )
}
