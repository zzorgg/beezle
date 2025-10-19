package com.github.zzorgg.beezle.ui.screens.wallet

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.zzorgg.beezle.data.wallet.SolanaWalletManager
import com.github.zzorgg.beezle.data.wallet.WalletState
import com.github.zzorgg.beezle.ui.components.EphemeralGreenTick
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import java.util.Locale

@Composable
fun WalletScreenRoot(
    sender: ActivityResultSender,
    navigateBackCallback: () -> Unit,
    modifier: Modifier = Modifier,
    walletManager: SolanaWalletManager = viewModel(),
) {
    val walletState by walletManager.walletState.collectAsState()
    val showTick = remember { mutableStateOf(false) }
    val prevConnected = remember { mutableStateOf(false) }

    LaunchedEffect(walletState.isConnected, walletState.wasRestored) {
        if (walletState.isConnected && !walletState.wasRestored && !prevConnected.value) {
            showTick.value = true
        }
        prevConnected.value = walletState.isConnected
    }

    Box {
        val blurModifier = if (showTick.value) Modifier.blur(22.dp) else Modifier
        Box(modifier = blurModifier) {
            WalletScreen(
                walletState = walletState,
                connectWalletCallback = { walletManager.connectWallet(sender) },
                disconnectWalletCallback = { walletManager.disconnectWallet(sender) },
                clearErrorCallback = { walletManager.clearError() },
                testSignMessageCallback = {
                    walletManager.signMessage(
                        sender,
                        "Hello from Beezle! Testing message signing."
                    )
                },
                navigateBackCallback = navigateBackCallback,
                modifier = modifier,
            )
        }
        EphemeralGreenTick(
            visible = showTick.value,
            fullScreen = true,
            modifier = Modifier.matchParentSize(),
            onFinished = { showTick.value = false }
        )
    }
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
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier
                .padding(it)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        text = "Wallet Status",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (walletState.isConnected) {
                        Text(
                            text = "Connected to ${walletState.walletName ?: "Phantom"}",
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Address: " + (walletState.publicKey?.let { "${it.take(8)}...${it.takeLast(8)}" } ?: "—"),
//                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val bal = walletState.balance
                        val balanceText = if (bal != null) String.format(Locale.US, "%.4f SOL", bal) else "— SOL"
                        Text(
                            text = "Balance: $balanceText",
//                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "No wallet connected",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Connect your Phantom wallet to get started",
//                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
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
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = clearErrorCallback, modifier = Modifier.fillMaxWidth()) {
                            Text("Dismiss", color = MaterialTheme.colorScheme.error)
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
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
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

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WalletScreenPreview_Connected() {
    BeezleTheme {
        WalletScreen(
            walletState = WalletState(
                isConnected = true,
                publicKey = "adsadadasad",
                authToken = "adasdadsada",
                walletName = "asdadsa",
                balance = 12.0,
                isLoading = false,
                error = "null",
                wasRestored = false
            ),
            connectWalletCallback = {},
            disconnectWalletCallback = {},
            clearErrorCallback = {},
            testSignMessageCallback = {},
            navigateBackCallback = {},
        )
    }
}