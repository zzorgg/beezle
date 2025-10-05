package com.github.zzorgg.beezle.ui.screens.duel.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import kotlinx.coroutines.delay


@Composable
fun SearchingScreen(
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
                CircularProgressIndicator()
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Finding Opponent...",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Please wait while we match you\nwith another player",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Do not show raw queue position; only elapsed wait to avoid confusion
        val waitedText = if (queueSince != null) "Waiting: ${'$'}{elapsedSeconds}s" else "Searching for the best match..."

        Text(
            text = waitedText,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
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
            ) {
                Text(
                    text = "Cancel Search",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SearchingScreenPreview() {
    BeezleTheme {
        SearchingScreen({}, 3, 121312)
    }
}