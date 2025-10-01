package com.github.zzorgg.beezle.ui.screens.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.github.zzorgg.beezle.data.wallet.SolanaWalletManager
import com.github.zzorgg.beezle.data.wallet.WalletState
import com.github.zzorgg.beezle.ui.theme.AccentGreen
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import com.github.zzorgg.beezle.ui.theme.SurfaceDark
import com.github.zzorgg.beezle.ui.theme.TextPrimary
import com.github.zzorgg.beezle.ui.theme.TextSecondary
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender

@Composable
fun WalletScreenRoot(
    sender: ActivityResultSender,
    navController: NavController,
    modifier: Modifier = Modifier,
    walletManager: SolanaWalletManager = viewModel(),
) {
    val walletState by walletManager.walletState.collectAsState()
    val hasNavigatedAfterConnection = remember { mutableStateOf(false) }

    // Only navigate to main screen when wallet gets freshly connected (not restored)
    LaunchedEffect(walletState.isConnected, walletState.wasRestored) {
        if (walletState.isConnected && !walletState.wasRestored && !hasNavigatedAfterConnection.value) {
            hasNavigatedAfterConnection.value = true
            navController.navigate("main") {
                popUpTo("wallet") { inclusive = true }
            }
        }
    }

    WalletScreen(
        walletState = walletState,
        connectWalletCallback = {
            hasNavigatedAfterConnection.value = false // Reset flag before new connection attempt
            walletManager.connectWallet(sender)
        },
        disconnectWalletCallback = { walletManager.disconnectWallet(sender) },
        clearErrorCallback = { walletManager.clearError() },
        testSignMessageCallback = {
            walletManager.signMessage(
                sender,
                "Hello from Beezle! Testing message signing."
            )
        },
        navigateBackCallback = {
            navController.navigate("main") {
                popUpTo("wallet") { inclusive = true }
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    walletState: WalletState,
    connectWalletCallback: () -> Unit,
    disconnectWalletCallback: () -> Unit,
    clearErrorCallback: () -> Unit,
    testSignMessageCallback: () -> Unit,
    navigateBackCallback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title ={},
                navigationIcon = {
                    IconButton(onClick = navigateBackCallback) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier
                .padding(it)
                .padding(8.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Wallet Status",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (walletState.isConnected) {
                        Text(
                            text = "Connected to ${walletState.walletName}",
                            color = AccentGreen,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Address: ${walletState.publicKey?.take(8)}...${
                                walletState.publicKey?.takeLast(
                                    8
                                )
                            }",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Balance: ${walletState.balance ?: 0.0} SOL",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "No wallet connected",
                            color = Color.Red,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Connect your Phantom wallet to get started",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        // Connect/Disconnect Button
                        if (!walletState.isConnected) {
                            FilledTonalButton(
                                onClick = connectWalletCallback,
                                modifier = Modifier.fillMaxWidth(0.85f),
                                enabled = !walletState.isLoading,
                            ) {
                                if (walletState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = when {
                                        walletState.isLoading -> "Connecting..."
                                        else -> "Connect Phantom Wallet"
                                    }
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = disconnectWalletCallback,
                                modifier = Modifier.fillMaxWidth(0.85f),
                            ) {
                                Text(text = "Disconnect Wallet")
                            }
                        }
                    }
                }
            }

            // Error Display
            walletState.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Error",
                            color = Color.Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = clearErrorCallback) {
                            Text("Dismiss", color = Color.Red)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Test Sign Message Button (only if connected)
            if (walletState.isConnected) {
                OutlinedButton(
                    onClick = testSignMessageCallback,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Sign Message")
                }
            }
        }
    }
}

@Preview
@Composable
private fun WalletScreenPreview() {
    BeezleTheme {
        WalletScreen(
            walletState = WalletState(),
            connectWalletCallback = {},
            disconnectWalletCallback = {},
            clearErrorCallback = {},
            testSignMessageCallback = {},
            navigateBackCallback = {},
        )
    }
}