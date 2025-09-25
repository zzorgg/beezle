package com.example.beezle.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
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
import com.example.beezle.ui.components.GradientButton
import com.example.beezle.ui.components.ModernCard
import com.example.beezle.ui.theme.*
import com.example.beezle.wallet.SolanaWalletManager
import androidx.activity.ComponentActivity

@Composable
fun WalletScreen(
    onWalletConnected: () -> Unit = {}
) {
    val walletManager: SolanaWalletManager = viewModel()
    val walletState by walletManager.walletState.collectAsState()
    val context = LocalContext.current

    // Content animation
    val contentAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, delayMillis = 300),
        label = "content_alpha"
    )

    // Show success message when connected
    LaunchedEffect(walletState.isConnected) {
        if (walletState.isConnected) {
            onWalletConnected()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundDark
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
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = if (walletState.isConnected) AccentGreen else PrimaryBlue,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (walletState.isConnected) Icons.Default.CheckCircle else Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.White,
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
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (walletState.isConnected)
                            "Your ${walletState.walletName} wallet is now connected to Beezle"
                        else
                            "Connect your Solana wallet to start earning SOL through competitive duels",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        ),
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Show wallet info if connected
                if (walletState.isConnected) {
                    ModernCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Wallet Address",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = walletState.publicKey?.take(8) + "..." + walletState.publicKey?.takeLast(8),
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Balance",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${walletState.balance ?: 0.0} SOL",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Wallet Options Cards
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Connect Existing Wallet Card
                        ModernCard(
                            onClick = {
                                walletManager.connectWallet(context as ComponentActivity)
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = PrimaryBlue.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Connect Phantom Wallet",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Connect to Phantom, Solflare, or other MWA wallets",
                                        color = TextSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // Sign In with Solana Card
                        ModernCard(
                            onClick = {
                                walletManager.signInWithSolana(context as ComponentActivity)
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = AccentGreen.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = AccentGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Sign In with Solana",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Secure sign-in with wallet verification",
                                        color = TextSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Error Display
                walletState.error?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    ModernCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = error,
                                color = androidx.compose.ui.graphics.Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Clear error button
                    TextButton(
                        onClick = { walletManager.clearError() }
                    ) {
                        Text("Dismiss", color = TextTertiary)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Security Note
                ModernCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Your wallet stays secure. We never store your private keys.",
                            color = TextSecondary,
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
                            walletManager.disconnectWallet(context as ComponentActivity)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        )
                    ) {
                        Text("Disconnect Wallet")
                    }
                } else {
                    GradientButton(
                        text = if (walletState.isLoading) "Connecting..." else "Connect Wallet",
                        onClick = {
                            walletManager.connectWallet(context as ComponentActivity)
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
                            color = TextTertiary,
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
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PrimaryBlue
                    )
                }
            }
        }
    }
}
