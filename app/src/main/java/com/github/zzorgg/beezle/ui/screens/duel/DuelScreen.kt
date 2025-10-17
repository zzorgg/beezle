package com.github.zzorgg.beezle.ui.screens.duel

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiObjects
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.zzorgg.beezle.data.model.duel.DuelMode
import com.github.zzorgg.beezle.data.model.duel.DuelState
import com.github.zzorgg.beezle.ui.screens.duel.components.ConnectionStatusIndicator
import com.github.zzorgg.beezle.ui.screens.duel.components.MatchFoundScreen
import com.github.zzorgg.beezle.ui.screens.duel.components.SearchingScreen
import com.github.zzorgg.beezle.ui.screens.duel.components.gameplay.AnswerButton
import com.github.zzorgg.beezle.ui.screens.duel.components.gameplay.QuestionCard
import com.github.zzorgg.beezle.ui.screens.duel.components.gameplay.TimerCircle
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
        QuestionCard(question = question)

        Spacer(Modifier.height(24.dp))

        // Answer Options
        question.options.forEachIndexed { index, option ->
            AnswerButton(
                text = option,
                isSelected = duelState.selectedAnswer == index,
                isCorrect = duelState.hasAnswered && index == question.correctAnswer,
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
        // Doesn't make sense...
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
