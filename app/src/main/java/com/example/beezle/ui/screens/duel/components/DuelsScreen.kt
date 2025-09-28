package com.example.beezle.ui.screens.duel.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beezle.ui.theme.BackgroundDark
import com.example.beezle.ui.theme.PrimaryBlue
import com.example.beezle.ui.theme.SurfaceDark
import com.example.beezle.ui.theme.TextPrimary
import com.example.beezle.ui.theme.TextSecondary
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
    val range = when(level) { in 1..2 -> 1..10; in 3..4 -> 5..25; else -> 10..50 }
    var a = Random.nextInt(range.first, range.last)
    var b = Random.nextInt(range.first, range.last)
    if (op == MathOp.DIV) {
        // ensure divisible
        b = listOf(1,2,3,4,5).random()
        a = b * Random.nextInt(1,10)
    }
    val answer = when(op){
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
    val (q,a) = csConcepts.random()
    return Question(q, a.lowercase(), Category.CS)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuelsScreen(navController: NavController) {
    var level by remember { mutableStateOf(1) }
    var current by remember { mutableStateOf<Question?>(null) }
    var userAnswer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }
    var streak by remember { mutableStateOf(0) }
    var category by remember { mutableStateOf(Category.MATH) }

    fun nextQuestion() {
        feedback = null
        userAnswer = ""
        current = if (category == Category.MATH) generateMathQuestion(level) else generateCsQuestion()
    }

    LaunchedEffect(Unit) { nextQuestion() }

    Surface(modifier = Modifier.fillMaxSize(), color = BackgroundDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextPrimary)
                }
                Text("Duels Practice", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Text("Prototype practice mode. Real-time matchmaking & escrow coming soon.", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(24.dp))

            // Category Switch
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = category==Category.MATH, onClick = { category = Category.MATH; nextQuestion() }, label = { Text("Math") })
                FilterChip(selected = category==Category.CS, onClick = { category = Category.CS; nextQuestion() }, label = { Text("CS") })
            }
            Spacer(Modifier.height(16.dp))

            current?.let { q ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(if (q.category==Category.MATH) "Math Question (Level $level)" else "CS Concept", color = TextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(q.prompt, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = userAnswer,
                            onValueChange = { userAnswer = it },
                            label = { Text("Your Answer") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                focusedLabelColor = PrimaryBlue
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = {
                                val normUser = userAnswer.trim().lowercase()
                                val correct = q.answer.trim().lowercase()
                                if (normUser == correct) {
                                    feedback = "✅ Correct!"
                                    streak += 1
                                    if (streak % 3 == 0) level += 1
                                } else {
                                    feedback = "❌ Incorrect. Answer: ${q.answer}";
                                    streak = 0
                                }
                            }, enabled = userAnswer.isNotBlank()) { Text("Check") }
                            OutlinedButton(onClick = { nextQuestion() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Skip")
                            }
                        }
                        feedback?.let { fb ->
                            Spacer(Modifier.height(12.dp))
                            Text(fb, color = if (fb.startsWith("✅")) Color(0xFF4CAF50) else Color(0xFFE57373))
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Session Stats", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Level: $level", color = TextPrimary)
                    Text("Streak: $streak", color = TextPrimary)
                    Text("Category: ${category.name}", color = TextPrimary)
                }
            }
        }
    }
}
