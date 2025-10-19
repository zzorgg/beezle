package com.github.zzorgg.beezle

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.zzorgg.beezle.ui.navigation.RootNavHost
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var sender: ActivityResultSender

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sender = ActivityResultSender(this)
        enableEdgeToEdge()
        setContent {
            BeezleTheme {
                val mainViewModel: MainViewModel = viewModel()

                RootNavHost(
                    mainViewModel = mainViewModel,
                    sender = sender,
                )
            }
        }
    }
}
