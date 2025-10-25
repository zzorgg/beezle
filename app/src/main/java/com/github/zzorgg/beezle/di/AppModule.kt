package com.github.zzorgg.beezle.di

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.github.zzorgg.beezle.data.local.LocalDataStoreRepository
import com.github.zzorgg.beezle.data.remote.DuelWebSocketService
import com.github.zzorgg.beezle.data.repository.AuthRepository
import com.github.zzorgg.beezle.data.repository.DuelRepository
import com.github.zzorgg.beezle.data.repository.FirebaseAuthRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


private const val DATASTORE_NAME = "breeze_data_store"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = DATASTORE_NAME
)

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideLocalDatastoreRepository(@ApplicationContext context: Context): LocalDataStoreRepository {
        return LocalDataStoreRepository(context.dataStore)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideCredentialManager(
        @ApplicationContext context: Context
    ): CredentialManager = CredentialManager.create(context)

    @Provides
    @Singleton
    fun provideDuelWebSocketService(): DuelWebSocketService {
        return DuelWebSocketService()
    }

    @Provides
    @Singleton
    fun provideDuelRepository(
        webSocketService: DuelWebSocketService,
        authRepository: AuthRepository,
    ): DuelRepository {
        return DuelRepository(webSocketService, authRepository)
    }


    @Provides
    @Singleton
    fun provideFirebaseAuthRepository(
        auth: FirebaseAuth,
        credentialManager: CredentialManager,
        @ApplicationContext context: Context,
    ): AuthRepository = FirebaseAuthRepository(auth, credentialManager, context)
}