package com.github.zzorgg.beezle.ui.screens.duel

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.airbnb.lottie.compose.*
import com.github.zzorgg.beezle.data.model.duel.DuelState
import com.github.zzorgg.beezle.data.model.duel.ConnectionStatus
import com.github.zzorgg.beezle.ui.screens.duel.components.GameplayScreen
import com.github.zzorgg.beezle.ui.screens.duel.components.PlayerCard
import com.github.zzorgg.beezle.ui.theme.BackgroundDark
import com.github.zzorgg.beezle.ui.theme.PrimaryBlue
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuelScreen(
    onNavigateBack: () -> Unit,
    viewModel: DuelViewModel = hiltViewModel()
) {
    val duelState by viewModel.duelState.collectAsState()
    var username by remember { mutableStateOf("") }
    var showUsernameDialog by remember { mutableStateOf(false) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar (removed title per request)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
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

            Spacer(modifier = Modifier.height(24.dp))

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
                            if (username.isBlank()) {
                                showUsernameDialog = true
                            } else {
                                viewModel.startDuel(username)
                            }
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

    // Username Dialog
    if (showUsernameDialog) {
        AlertDialog(
            onDismissRequest = { showUsernameDialog = false },
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
                            showUsernameDialog = false
                            viewModel.startDuel(username)
                        }
                    }
                ) {
                    Text("START DUEL")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUsernameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ConnectionStatusIndicator(status: ConnectionStatus) {
    val color = when (status) {
        ConnectionStatus.CONNECTED -> Color.Green
        ConnectionStatus.CONNECTING, ConnectionStatus.RECONNECTING -> Color.Yellow
        ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> Color.Red
    }

    val scale by animateFloatAsState(
        targetValue = if (status == ConnectionStatus.CONNECTING || status == ConnectionStatus.RECONNECTING) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = Modifier
            .size(12.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun StartDuelScreen(onStartDuel: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Battle Animation using provided Rrc3wq5CfZ.json
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Rrc3wq5CfZ.json"))
        val progress by animateLottieCompositionAsState(
            composition,
            iterations = LottieConstants.IterateForever,
            speed = 1f,
            restartOnPlay = false
        )

        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            if (composition != null) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Removed fallback TwoSwordsIcon; simple Box placeholder
                Box(
                    modifier = Modifier
                        .size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Ready to Duel?",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Test your knowledge against other players\nin fast-paced competitive rounds!",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White.copy(alpha = 0.8f)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStartDuel,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                text = "START DUEL",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SearchingScreen(
    onCancel: () -> Unit,
    queuePosition: Int?,
    queueSince: Long?
) {
    val elapsedSeconds by produceState(initialValue = 0L, key1 = queueSince) {
        while (true) {
            queueSince?.let { value = (System.currentTimeMillis() - it) / 1000 }
            delay(1000)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Lottie searching animation using provided asset 0cvjnffoJq.json
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("0cvjnffoJq.json"))
        val progress by animateLottieCompositionAsState(
            composition,
            iterations = LottieConstants.IterateForever,
            speed = 1f,
            restartOnPlay = false
        )

        Box(
            modifier = Modifier
                .size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            if (composition != null) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress }
                )
            } else {
                // Fallback shimmer / placeholder while loading composition
                CircularProgressIndicator(color = Color(0xFFE94560))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Finding Opponent...",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Please wait while we match you\nwith another player",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White.copy(alpha = 0.8f)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        val positionText = queuePosition?.let { "Your position in queue: #$it" } ?: "Joining queue..."
        val waitedText = if (queueSince != null) "Waiting: ${elapsedSeconds}s" else ""

        Text(
            text = positionText + if (waitedText.isNotEmpty()) "\n$waitedText" else "",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White.copy(alpha = 0.8f)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Replaced cancel button section with unified button style
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(
                    text = "Cancel Search",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun WaitingForQuestionScreen(duelState: DuelState) {
    duelState.currentRoom?.let { room ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Match Found!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Player vs Player UI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerCard(
                    username = room.player1.username,
                    isCurrentPlayer = true
                )

                Text(
                    text = "VS",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE94560)
                    )
                )

                PlayerCard(
                    username = room.player2?.username ?: "Unknown",
                    isCurrentPlayer = false
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Get Ready!",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White.copy(alpha = 0.8f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
