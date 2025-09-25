package com.example.beezle

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.beezle.ui.theme.*
import com.example.beezle.wallet.SolanaWalletManager
import androidx.activity.ComponentActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val walletManager: SolanaWalletManager = viewModel()
    val walletState by walletManager.walletState.collectAsState()
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Beezle",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        if (walletState.isConnected) {
                            walletManager.disconnectWallet(context as ComponentActivity)
                        } else {
                            walletManager.connectWallet(context as ComponentActivity)
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (walletState.isConnected) Icons.Default.AccountBalanceWallet else Icons.Default.Warning,
                        contentDescription = "Wallet Status",
                        tint = if (walletState.isConnected) AccentGreen else androidx.compose.ui.graphics.Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Wallet Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
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
                            text = "✅ Connected to ${walletState.walletName}",
                            color = AccentGreen,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Address: ${walletState.publicKey?.take(8)}...${walletState.publicKey?.takeLast(8)}",
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
                            text = "❌ No wallet connected",
                            color = androidx.compose.ui.graphics.Color.Red,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Connect your Phantom wallet to get started",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Connect/Disconnect Button
            Button(
                onClick = {
                    if (walletState.isConnected) {
                        walletManager.disconnectWallet(context as ComponentActivity)
                    } else {
                        walletManager.connectWallet(context as ComponentActivity)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !walletState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (walletState.isConnected) androidx.compose.ui.graphics.Color.Red else PrimaryBlue
                )
            ) {
                if (walletState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = androidx.compose.ui.graphics.Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = when {
                        walletState.isLoading -> "Connecting..."
                        walletState.isConnected -> "Disconnect Wallet"
                        else -> "Connect Phantom Wallet"
                    }
                )
            }

            // Error Display
            walletState.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Error",
                            color = androidx.compose.ui.graphics.Color.Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = androidx.compose.ui.graphics.Color.Red,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { walletManager.clearError() }
                        ) {
                            Text("Dismiss", color = androidx.compose.ui.graphics.Color.Red)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Test Sign Message Button (only if connected)
            if (walletState.isConnected) {
                OutlinedButton(
                    onClick = {
                        walletManager.signMessage(
                            context as ComponentActivity,
                            "Hello from Beezle! Testing message signing."
                        )
                    },
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
