package com.example.beezle

    import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.beezle.ui.theme.AccentGreen
import com.example.beezle.ui.theme.BackgroundDark
import com.example.beezle.ui.theme.PrimaryBlue
import com.example.beezle.ui.theme.SurfaceDark
import com.example.beezle.ui.theme.TextPrimary
import com.example.beezle.ui.theme.TextSecondary
import com.example.beezle.wallet.SolanaWalletManager
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(sender: ActivityResultSender) {
    val walletManager: SolanaWalletManager = viewModel()
    val walletState by walletManager.walletState.collectAsState()

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
                            walletManager.disconnectWallet(sender)
                        } else {
                            walletManager.connectWallet(sender)
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
                        walletManager.disconnectWallet(sender)
                    } else {
                        walletManager.connectWallet(sender)
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
