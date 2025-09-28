package com.example.beezle.ui.screens.duel

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.beezle.data.model.duel.DuelState
import com.example.beezle.data.model.duel.ConnectionStatus
import com.example.beezle.ui.components.GradientButton
import com.example.beezle.ui.screens.duel.components.GameplayScreen
import com.example.beezle.ui.screens.duel.components.PlayerCard
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "DUEL ARENA",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                ConnectionStatusIndicator(duelState.connectionStatus)
            }

            Spacer(modifier = Modifier.height(32.dp))

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
                    SearchingScreen(onCancel = viewModel::leaveQueue)
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
        // Animated Duel Icon
        val infiniteTransition = rememberInfiniteTransition(label = "")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )

        Icon(
            Icons.Default.Sports,
            contentDescription = "Duel",
            modifier = Modifier
                .size(120.dp)
                .scale(scale),
            tint = Color(0xFFE94560)
        )

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
            text = "Test your knowledge against other players\nin fast-paced 15-second rounds!",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White.copy(alpha = 0.8f)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        GradientButton(
            text = "START DUEL",
            onClick = onStartDuel,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp),
            gradient = Brush.horizontalGradient(
                colors = listOf(Color(0xFFE94560), Color(0xFFF27121))
            )
        )
    }
}

@Composable
private fun SearchingScreen(onCancel: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Spinning search animation
        val infiniteTransition = rememberInfiniteTransition(label = "")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing)
            ), label = ""
        )

        Icon(
            Icons.Default.Search,
            contentDescription = "Searching",
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer { rotationZ = rotation },
            tint = Color(0xFFE94560)
        )

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

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedButton(
            onClick = onCancel,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            )
        ) {
            Text("Cancel Search")
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
