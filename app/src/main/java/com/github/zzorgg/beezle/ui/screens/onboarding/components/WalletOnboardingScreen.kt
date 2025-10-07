package com.github.zzorgg.beezle.ui.screens.onboarding.components

import android.accounts.AccountManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.zzorgg.beezle.ui.components.GradientButton
import com.github.zzorgg.beezle.data.wallet.SolanaWalletManager
import com.github.zzorgg.beezle.ui.screens.profile.NoGoogleAccountScreen
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import java.util.Locale

@Composable
fun WalletOnboardingScreen(
    onWalletConnected: () -> Unit = {},
    sender: ActivityResultSender
) {
    val walletManager: SolanaWalletManager = viewModel()
    val walletState by walletManager.walletState.collectAsState()
    val context = LocalContext.current
    var showNoGoogleAccountDialog by remember { mutableStateOf(false) }

    if (showNoGoogleAccountDialog) {
        NoGoogleAccountScreen()
        return
    }

    // Content animation
    val contentAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, delayMillis = 300),
        label = "content_alpha"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Use theme background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(contentAlpha)
                    .padding(24.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Header Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Wallet Icon with connection status
                    val bgColor = if (walletState.isConnected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    val iconTint = if (walletState.isConnected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = bgColor,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (walletState.isConnected) Icons.Default.CheckCircle else Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = if (walletState.isConnected) "Wallet Connected!" else "Connect Wallet",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground, // better contrast on background
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (walletState.isConnected)
                            "Your ${walletState.walletName ?: "Phantom"} wallet is now connected to Beezle"
                        else
                            "Connect your Solana wallet to start earning SOL through competitive duels",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // secondary text on bg
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Show wallet info if connected
                if (walletState.isConnected) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Wallet Address",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = walletState.publicKey?.let { "${it.take(8)}...${it.takeLast(8)}" } ?: "—",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Balance",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val bal = walletState.balance
                            val balanceText = if (bal != null) String.format(Locale.US, "%.4f SOL", bal) else "— SOL"
                            Text(
                                text = balanceText,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Wallet Options (Phantom only)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Connect Phantom Wallet Card
                        Card(
                            onClick = {
                                val accountManager = AccountManager.get(context)
                                val accounts = accountManager.getAccountsByType("com.google")
                                if (accounts.isEmpty()) {
                                    showNoGoogleAccountDialog = true
                                } else {
                                    walletManager.connectWallet(sender)
                                }
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Connect Phantom Wallet",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Only Phantom is supported right now",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // Note under the card
                        Text(
                            text = "Note: We currently support Phantom only.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        // Removed: Sign In with Solana card (not supported now)
                    }
                }

                // Error Display
                walletState.error?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Clear error button
                    TextButton(
                        onClick = { walletManager.clearError() }
                    ) {
                        Text("Dismiss", color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Security Note
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Your wallet stays secure. We never store your private keys.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Primary Action Button
                if (walletState.isConnected) {
                    GradientButton(
                        text = "Continue to App",
                        onClick = onWalletConnected,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.CheckCircle
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            walletManager.disconnectWallet(sender)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Disconnect Wallet")
                    }
                } else {
                    GradientButton(
                        text = if (walletState.isLoading) "Connecting..." else "Connect Wallet",
                        onClick = {
                            walletManager.connectWallet(sender)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.AccountBalanceWallet,
                        enabled = !walletState.isLoading
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Skip for now option (only show if not connected)
                if (!walletState.isConnected) {
                    TextButton(
                        onClick = onWalletConnected,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Skip for now (Demo mode)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Loading overlay
            if (walletState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
