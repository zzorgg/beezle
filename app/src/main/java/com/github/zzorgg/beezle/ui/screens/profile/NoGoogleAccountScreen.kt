package com.github.zzorgg.beezle.ui.screens.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun NoGoogleAccountScreen() {
    val context = LocalContext.current
    // Keep using the asset you referenced; it exists at app/src/main/assets/Sign.json
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Sign.json"))
    val viewModel: ProfileViewModel = hiltViewModel()

    AlertDialog(
        onDismissRequest = { viewModel.acknowledgeNoGoogleAccountNotice() },
        title = { Text("Sign in with Google") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Constrain and fit the Lottie to the dialog width with an upper bound
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val maxAnimWidth = maxWidth * 0.8f
                    val clampedWidth = if (maxAnimWidth > 220.dp) 220.dp else maxAnimWidth
                    // If composition is loaded, preserve its aspect ratio; otherwise fallback square
                    val aspect = composition?.let { comp ->
                        val w = comp.bounds.width().toFloat().coerceAtLeast(1f)
                        val h = comp.bounds.height().toFloat().coerceAtLeast(1f)
                        (w / h).coerceIn(0.5f, 2.0f)
                    } ?: 1f

                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier
                            .width(clampedWidth)
                            .aspectRatio(aspect, matchHeightConstraintsFirst = true)
                            .sizeIn(maxHeight = 220.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }

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
