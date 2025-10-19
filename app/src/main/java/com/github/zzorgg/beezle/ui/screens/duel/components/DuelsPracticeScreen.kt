package com.github.zzorgg.beezle.ui.screens.duel.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import kotlin.random.Random

enum class Category { MATH, CS }
private enum class MathOp(val symbol: String) { ADD("+"), SUB("-"), MUL("*"), DIV("/") }

data class Question(
    val prompt: String,
    val answer: String,
    val category: Category,
    val meta: String = ""
)

private fun generateMathQuestion(level: Int): Question {
    val op = MathOp.entries.random()
    val range = when (level) {
        in 1..2 -> 1..10; in 3..4 -> 5..25; else -> 10..50
    }
    var a = Random.nextInt(range.first, range.last)
    var b = Random.nextInt(range.first, range.last)
    if (op == MathOp.DIV) {
        // ensure divisible
        b = listOf(1, 2, 3, 4, 5).random()
        a = b * Random.nextInt(1, 10)
    }
    val answer = when (op) {
        MathOp.ADD -> a + b
        MathOp.SUB -> a - b
        MathOp.MUL -> a * b
        MathOp.DIV -> a / b
    }.toString()
    return Question("$a ${op.symbol} $b = ?", answer, Category.MATH, meta = "op=${op.symbol}")
}

private val csConcepts = listOf(
    "What does O(n) mean?" to "linear complexity",
    "Data structure with FIFO order?" to "queue",
    "Binary tree with ordered nodes?" to "bst",
    "Hash collision resolution technique using linked lists?" to "chaining",
    "Algorithm to traverse graphs breadth-first?" to "bfs",
    "Sort algorithm with average O(n log n) using divide & conquer?" to "merge sort"
)

private fun generateCsQuestion(): Question {
    val (q, a) = csConcepts.random()
    return Question(q, a.lowercase(), Category.CS)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuelsPracticeScreenRoot(
    initialCategory: Category = Category.MATH,
    navigateBackCallback: () -> Unit,
) {
    var level by remember { mutableIntStateOf(1) }
    var current by remember { mutableStateOf<Question?>(null) }
    var userAnswer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }
    var streak by remember { mutableIntStateOf(0) }
    var category by remember { mutableStateOf(initialCategory) }

    fun nextQuestion() {
        feedback = null
        userAnswer = ""
        current =
            if (category == Category.MATH) generateMathQuestion(level) else generateCsQuestion()
    }

    LaunchedEffect(Unit, category) { nextQuestion() }

    DuelsPracticeScreen(
        level = level,
        current = current,
        userAnswer = userAnswer,
        userAnswerChangeCallback = { userAnswer = it },
        feedback = feedback,
        streak = streak,
        category = category,
        nextCategory = { category = it; nextQuestion() },
        checkButtonCallback = {
            if (current != null) {
                val normUser = userAnswer.trim().lowercase()
                val correct = current!!.answer.trim().lowercase()
                if (normUser == correct) {
                    feedback = "✅ Correct!"
                    streak += 1
                    if (streak % 3 == 0) level += 1
                } else {
                    feedback = "❌ Incorrect. Answer: ${current!!.answer}"
                    streak = 0
                }
            }
        },
        nextQuestionCallback = { nextQuestion() },
        navigateBackCallback = navigateBackCallback,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuelsPracticeScreen(
    level: Int,
    current: Question?,
    userAnswer: String,
    userAnswerChangeCallback: (String) -> Unit,
    feedback: String?,
    streak: Int,
    category: Category,
    nextCategory: (Category) -> Unit,
    checkButtonCallback: () -> Unit,
    nextQuestionCallback: () -> Unit,
    navigateBackCallback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsMartialArts,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            "Duels Practice",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = navigateBackCallback) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Prototype practice mode. Real-time matchmaking & escrow coming soon.",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))

            // Category Switch
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = category == Category.MATH,
                    onClick = { nextCategory(Category.MATH) },
                    label = { Text("Math") })
                FilterChip(
                    selected = category == Category.CS,
                    onClick = { nextCategory(Category.CS) },
                    label = { Text("CS") })
            }
            Spacer(Modifier.height(16.dp))

            current?.let { q ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            if (q.category == Category.MATH) "Math Question (Level $level)" else "CS Concept",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            q.prompt,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = userAnswer,
                            onValueChange = userAnswerChangeCallback,
                            label = { Text("Your Answer") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = checkButtonCallback,
                                enabled = userAnswer.isNotBlank()
                            ) { Text("Check") }
                            OutlinedButton(onClick = { nextQuestionCallback() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Skip")
                            }
                        }
                        feedback?.let { fb ->
                            Spacer(Modifier.height(12.dp))
                            Text(
                                fb,
                                color = if (fb.startsWith("✅")) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Session Stats",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Level: $level", color = MaterialTheme.colorScheme.onSurface)
                    Text("Streak: $streak", color = MaterialTheme.colorScheme.onSurface)
                    Text("Category: ${category.name}", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DuelsPracticeScreenPreview() {
    BeezleTheme {
        DuelsPracticeScreen(
            level = 2,
            current = Question(
                prompt = "What the what",
                answer = "What",
                category = Category.CS,
            ),
            userAnswer = "asdasd",
            userAnswerChangeCallback = { },
            feedback = "sdasda",
            streak = 2,
            category = Category.MATH,
            nextCategory = { },
            checkButtonCallback = {},
            nextQuestionCallback = {},
            navigateBackCallback = {}
        )
    }
}
