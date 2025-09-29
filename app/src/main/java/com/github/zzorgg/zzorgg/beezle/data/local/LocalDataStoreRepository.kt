package com.github.zzorgg.beezle.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

data class LocalData(
    val hasOnboarded: Boolean = false,
    val hasConnectedWallet: Boolean = false,
)

class LocalDataStoreRepository(private val dataStore: DataStore<Preferences>) {
    private companion object {
        val HAS_ONBOARDED = booleanPreferencesKey("has_user_onboarded")
        val HAS_CONNECTED_WALLET = booleanPreferencesKey("has_user_connected_wallet")
    }

    val localData: Flow<LocalData> = dataStore.data
        .catch {
            if (it is IOException) {
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {
            LocalData(
                hasOnboarded = it[HAS_ONBOARDED] ?: false,
                hasConnectedWallet = it[HAS_CONNECTED_WALLET] ?: false,
            )
        }

    // Can be used in view models
    suspend fun hasUserOnBoardedSnapshot(): Boolean = dataStore.data.first()[HAS_ONBOARDED] ?: false

    // Can be used in view models
    suspend fun hasUserConnectedWalletSnapshot(): Boolean = dataStore.data.first()[HAS_CONNECTED_WALLET] ?: false

    suspend fun update(data: LocalData) {
        dataStore.edit {
            it[HAS_ONBOARDED] = data.hasOnboarded
            it[HAS_CONNECTED_WALLET] = data.hasConnectedWallet
        }
    }
}