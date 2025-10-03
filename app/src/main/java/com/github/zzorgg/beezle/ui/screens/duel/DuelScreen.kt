package com.github.zzorgg.beezle.ui.screens.duel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.zzorgg.beezle.ui.screens.duel.components.ConnectionStatusIndicator
import com.github.zzorgg.beezle.ui.screens.duel.components.GameplayScreen
import com.github.zzorgg.beezle.ui.screens.duel.components.SearchingScreen
import com.github.zzorgg.beezle.ui.screens.duel.components.StartDuelScreen
import com.github.zzorgg.beezle.ui.screens.duel.components.WaitingForQuestionScreen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuelScreen(
    onNavigateBack: () -> Unit,
    viewModel: DuelViewModel = hiltViewModel(),
) {
    val duelState by viewModel.duelState.collectAsState()
    var username by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.connectToServer()
    }

    // Handle errors
    duelState.error?.let { error ->
        LaunchedEffect(error) {
            delay(3000)
            viewModel.clearError()
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar (removed title per request)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    ConnectionStatusIndicator(duelState.connectionStatus)
                }

                // Main Content
                when {
                    duelState.currentRoom != null && duelState.currentQuestion != null -> {
                        // In-game UI
                        GameplayScreen(
                            duelState = duelState,
                            onAnswerSelected = viewModel::submitAnswer,
                            onClearRoundResult = viewModel::clearLastRoundResult
                        )
                    }

                    duelState.currentRoom != null -> {
                        // Waiting for question
                        WaitingForQuestionScreen(duelState)
                    }

                    duelState.isSearching -> {
                        // Searching for opponent
                        SearchingScreen(
                            onCancel = viewModel::leaveQueue,
                            queuePosition = duelState.queuePosition,
                            queueSince = duelState.queueSince
                        )
                    }

                    else -> {
                        // Start screen
                        StartDuelScreen(
                            onStartDuel = {
                                viewModel.startDuel()
                            }
                        )
                    }
                }
            }

            // Error Snackbar
            duelState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.9f)
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Username Dialog
    if (false) {
        AlertDialog(
            onDismissRequest = { /*showUsernameDialog = false*/ },
            title = { Text("Enter Username") },
            text = {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (username.isNotBlank()) {
//                            showUsernameDialog = false
//                            viewModel.startDuel(username)
                        }
                    }
                ) {
                    Text("START DUEL")
                }
            },
            dismissButton = {
                TextButton(onClick = { /*showUsernameDialog = false*/ }) {
                    Text("Cancel")
                }
            }
        )
    }
}

