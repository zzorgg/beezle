package com.github.zzorgg.beezle.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.stylusHoverIcon
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.github.zzorgg.beezle.ui.theme.AccentGreen
import com.github.zzorgg.beezle.ui.theme.PrimaryBlue
import com.github.zzorgg.beezle.ui.theme.SurfaceDark
import com.github.zzorgg.beezle.ui.theme.TextPrimary
import com.github.zzorgg.beezle.ui.theme.TextSecondary
import com.github.zzorgg.beezle.data.wallet.SolanaWalletManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.github.zzorgg.beezle.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(sender: ActivityResultSender, navController: NavController) {
    val walletManager: SolanaWalletManager = viewModel()
    val walletState by walletManager.walletState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    if (walletState.isConnected) {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AccentGreen.copy(alpha = 0.15f))
                                .clickable { navController.navigate("profile") }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = walletState.publicKey?.take(4) + "..." + walletState.publicKey?.takeLast(
                                    4
                                ),
                                color = AccentGreen,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    IconButton(
                        onClick = {
                            if (walletState.isConnected) {
                                walletManager.disconnectWallet(sender)
                            } else {
                                walletManager.connectWallet(sender)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (walletState.isConnected) Icons.Default.AccountBalanceWallet else Icons.Default.Warning,
                            contentDescription = "Wallet Status",
                            tint = if (walletState.isConnected) AccentGreen else Color.Red
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp, 16.dp)
                .padding(innerPadding),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { navController.navigate("duels") },
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.SportsMartialArts,
                            contentDescription = null,
                            tint = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Duels", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Practice & compete", color = TextSecondary, fontSize = 12.sp)
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { navController.navigate("profile") },
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AccentGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Profile", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Stats & levels", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Wallet Status Card
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
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        // Connect/Disconnect Button
                        if (!walletState.isConnected) {
                            FilledTonalButton(
                                onClick = {
                                    walletManager.connectWallet(sender)
                                },
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
                                onClick = {
                                    walletManager.disconnectWallet(sender)
                                },
                                modifier = Modifier.fillMaxWidth(0.85f),
                            ) {
                                Text(text = "Disconnect Wallet")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))


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
                        TextButton(
                            onClick = { walletManager.clearError() }
                        ) {
                            Text("Dismiss", color = Color.Red)
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
                            sender,
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
