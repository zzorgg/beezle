package com.example.beezle.wallet.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

// Single source for wallet datastore
val Context.walletDataStore by preferencesDataStore(name = "wallet_prefs")

