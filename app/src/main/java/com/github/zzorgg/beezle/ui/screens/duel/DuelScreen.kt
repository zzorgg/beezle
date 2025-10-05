package com.github.zzorgg.beezle.ui.screens.duel

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.airbnb.lottie.compose.*
import com.github.zzorgg.beezle.data.model.duel.ConnectionStatus
import com.github.zzorgg.beezle.data.model.duel.DuelMode
import com.github.zzorgg.beezle.data.model.duel.DuelState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuelScreen(
    onNavigateBack: () -> Unit,
    initialMode: DuelMode? = null,
    viewModel: DuelViewModel = hiltViewModel()
) {
    val duelState by viewModel.duelState.collectAsState()
    var selectedMode by remember { mutableStateOf(initialMode ?: DuelMode.MATH) }

    LaunchedEffect(Unit) {
        viewModel.connectToServer()
    }

    // Auto-dismiss errors after 5 seconds
    duelState.error?.let { error ->
        LaunchedEffect(error) {
            delay(5000)
            viewModel.clearError()
        }
    }

    // Handle game over result
    duelState.lastGameResult?.let { result ->
        GameOverDialog(
            result = result,
            myId = viewModel.duelState.value.currentRoom?.player1?.id ?: "",
            onDismiss = {
                viewModel.clearGameResult()
                onNavigateBack()
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (duelState.isSearching) {
                            viewModel.leaveQueue()
                        }
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    ConnectionStatusIndicator(duelState.connectionStatus)
                }

                // Main Content
                when {
                    duelState.currentRoom != null && duelState.currentQuestion != null -> {
                        // Gameplay Screen
                        GameplayScreen(
                            duelState = duelState,
                            onAnswerSelected = viewModel::submitAnswer
                        )
                    }

                    duelState.currentRoom != null -> {
                        // Match found - waiting for first question
                        MatchFoundScreen(duelState = duelState)
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
                        // Mode selection and start
                        ModeSelectionScreen(
                            selectedMode = selectedMode,
                            onModeSelected = { selectedMode = it },
                            onStartDuel = {
                                // Username ignored; repository will use Firebase display name
                                viewModel.startDuel("", selectedMode)
                            },
                            isConnected = duelState.isConnected
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
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusIndicator(status: ConnectionStatus) {
    val dotColor = when (status) {
        ConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.tertiary
        ConnectionStatus.CONNECTING, ConnectionStatus.RECONNECTING -> MaterialTheme.colorScheme.secondary
        ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> MaterialTheme.colorScheme.error
    }

    val scale by animateFloatAsState(
        targetValue = if (status == ConnectionStatus.CONNECTING || status == ConnectionStatus.RECONNECTING) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ), label = "statusPulse"
    )

    Box(
        modifier = Modifier
            .size(12.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(dotColor)
    )
}

@Composable
private fun ModeSelectionScreen(
    selectedMode: DuelMode,
    onModeSelected: (DuelMode) -> Unit,
    onStartDuel: () -> Unit,
    isConnected: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Battle Animation
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Rrc3wq5CfZ.json"))
        val progress by animateLottieCompositionAsState(
            composition,
            iterations = LottieConstants.IterateForever
        )

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(180.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Choose Your Challenge",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select a category and compete in real-time!",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Mode Selection Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModeCard(
                mode = DuelMode.MATH,
                isSelected = selectedMode == DuelMode.MATH,
                onClick = { onModeSelected(DuelMode.MATH) },
                modifier = Modifier.weight(1f)
            )
            ModeCard(
                mode = DuelMode.CS,
                isSelected = selectedMode == DuelMode.CS,
                onClick = { onModeSelected(DuelMode.CS) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Start Duel Button
        Button(
            onClick = onStartDuel,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedMode == DuelMode.MATH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            ),
            enabled = isConnected
        ) {
            Icon(
                Icons.Default.SportsMartialArts,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isConnected) "START DUEL" else "CONNECTING...",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun ModeCard(
    mode: DuelMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor: Color = when (mode) {
        DuelMode.MATH -> MaterialTheme.colorScheme.primary
        DuelMode.CS -> MaterialTheme.colorScheme.tertiary
        DuelMode.GENERAL -> MaterialTheme.colorScheme.secondary
    }

    val icon = when (mode) {
        DuelMode.MATH -> Icons.Default.Calculate
        DuelMode.CS -> Icons.Default.Code
        DuelMode.GENERAL -> Icons.Default.EmojiObjects
    }

    val label = when (mode) {
        DuelMode.MATH -> "Math Duel"
        DuelMode.CS -> "CS Duel"
        DuelMode.GENERAL -> "Mixed"
    }

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(3.dp, accentColor, RoundedCornerShape(16.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = label,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center
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
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Searching animation
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("0cvjnffoJq.json"))
        val progress by animateLottieCompositionAsState(
            composition,
            iterations = LottieConstants.IterateForever
        )

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Finding Opponent...",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Do not show raw queue position to avoid confusion; show elapsed wait time instead
        if (elapsedSeconds > 0) {
            Text(
                text = "Waiting: ${'$'}{elapsedSeconds}s",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        } else {
            Text(
                text = "Searching for the best match...",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Cancel Search")
        }
    }
}

@Composable
private fun MatchFoundScreen(duelState: DuelState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Match Found!",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        duelState.currentRoom?.let { room ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PlayerBadge(
                    name = room.player1.username,
                    score = duelState.myScore,
                    isYou = true
                )

                Text(
                    text = "VS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                PlayerBadge(
                    name = room.player2?.username ?: "Opponent",
                    score = duelState.opponentScore,
                    isYou = false
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Get ready...",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlayerBadge(
    name: String,
    score: Int,
    isYou: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background((if (isYou) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary).copy(alpha = 0.2f))
                .border(2.dp, if (isYou) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(2).uppercase(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (isYou) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = name,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (isYou) {
            Text(
                text = "(You)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "Score: $score",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GameplayScreen(
    duelState: DuelState,
    onAnswerSelected: (Int) -> Unit
) {
    val question = duelState.currentQuestion ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Score Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ScoreCard(
                name = "You",
                score = duelState.myScore,
                color = MaterialTheme.colorScheme.primary,
                hasAnswered = duelState.hasAnswered
            )

            Text(
                text = "Round ${duelState.currentRound}/${duelState.totalRounds}",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            ScoreCard(
                name = "Opponent",
                score = duelState.opponentScore,
                color = MaterialTheme.colorScheme.secondary,
                hasAnswered = duelState.opponentAnswered
            )
        }

        Spacer(Modifier.height(16.dp))

        // Timer
        TimerCircle(timeRemaining = duelState.timeRemaining)

        Spacer(Modifier.height(24.dp))

        // Question
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = question.text,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Answer Options
        question.options.forEachIndexed { index, option ->
            AnswerButton(
                text = option,
                isSelected = duelState.selectedAnswer == index,
                isCorrect = duelState.hasAnswered && index == question.correctAnswer,
                isWrong = duelState.hasAnswered && duelState.selectedAnswer == index && index != question.correctAnswer,
                enabled = !duelState.hasAnswered,
                onClick = { onAnswerSelected(index) }
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ScoreCard(
    name: String,
    score: Int,
    color: Color,
    hasAnswered: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = score.toString(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        if (hasAnswered) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Answered",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun TimerCircle(timeRemaining: Int) {
    val progress = timeRemaining / 15f
    val ringColor = when {
        timeRemaining > 10 -> MaterialTheme.colorScheme.tertiary
        timeRemaining > 5 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(80.dp)
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = ringColor,
            strokeWidth = 6.dp
        )
        Text(
            text = timeRemaining.toString(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = ringColor
        )
    }
}

@Composable
private fun AnswerButton(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bg: Color = when {
        isCorrect -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
        isWrong -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }

    val border: Color = when {
        isCorrect -> MaterialTheme.colorScheme.tertiary
        isWrong -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .then(if (border != Color.Transparent) Modifier.border(2.dp, border, RoundedCornerShape(12.dp)) else Modifier),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            disabledContainerColor = bg
        ),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            if (isCorrect) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            } else if (isWrong) {
                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun GameOverDialog(
    result: com.github.zzorgg.beezle.data.model.duel.WebSocketMessage.GameOver,
    myId: String,
    onDismiss: () -> Unit
) {
    val isWinner = result.data.winner_id == myId
    val myScore = result.data.scores[myId] ?: 0
    val opponentScore = result.data.scores.filterKeys { it != myId }.values.firstOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isWinner) "🏆 Victory!" else "💪 Good Try!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Final Score",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("You", fontWeight = FontWeight.Bold)
                        Text(
                            myScore.toString(),
                            fontSize = 32.sp,
                            color = if (isWinner) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Opponent", fontWeight = FontWeight.Bold)
                        Text(
                            opponentScore.toString(),
                            fontSize = 32.sp,
                            color = if (!isWinner) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWinner) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Done")
            }
        }
    )
}
