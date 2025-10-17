package com.github.zzorgg.beezle.ui.screens.leaderboards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.zzorgg.beezle.ui.components.AppBottomBar
import com.github.zzorgg.beezle.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardsScreen(onNavigate: (Route) -> Unit) {
    val density = LocalDensity.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaderboards") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        },
        floatingActionButton = { AppBottomBar(currentRoute = Route.Leaderboard, onNavigate = onNavigate) },
        floatingActionButtonPosition = FabPosition.Center,
        contentWindowInsets = WindowInsets(
            top = WindowInsets.systemBars.getTop(density),
            left = WindowInsets.systemBars.getLeft(density, LocalLayoutDirection.current),
            right = WindowInsets.systemBars.getRight(density, LocalLayoutDirection.current),
            bottom = WindowInsets.systemBars.getBottom(density) / 3
        )
    ) { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Leaderboards",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Coming soon",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
