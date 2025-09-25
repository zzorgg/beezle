package com.example.beezle.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beezle.onboarding.OnboardingSlide

@Composable
fun TestOnboardingSlide() {
    OnboardingSlide(
        title = "Test Title",
        description = "Test Description",
        icon = {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎯", fontSize = 64.sp)
            }
        }
    )
}
