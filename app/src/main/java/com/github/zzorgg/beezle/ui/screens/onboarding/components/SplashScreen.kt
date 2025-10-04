package com.github.zzorgg.beezle.ui.screens.onboarding.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.material3.MaterialTheme

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(Unit) {
        val bmp = withContext(Dispatchers.IO) {
            try {
                context.assets.open("beezle.png").use { input ->
                    BitmapFactory.decodeStream(input)
                }
            } catch (_: Exception) { null }
        }
        imageBitmap = bmp?.asImageBitmap()
        // Keep a short delay (retain previous timing behavior)
        // Maybe load signed in user here
        delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        imageBitmap?.let { bmp ->
            Image(
                bitmap = bmp,
                contentDescription = "Beezle logo",
                modifier = Modifier.size(300.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Preview
@Composable
fun PreviewSplashScreen() {
    SplashScreen(onFinished = {})
}
