package com.example.beezle.duel.di

import com.example.beezle.duel.network.DuelWebSocketService
import com.example.beezle.duel.repository.DuelRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DuelModule {

    @Provides
    @Singleton
    fun provideDuelWebSocketService(): DuelWebSocketService {
        return DuelWebSocketService()
    }

    @Provides
    @Singleton
    fun provideDuelRepository(
        webSocketService: DuelWebSocketService
    ): DuelRepository {
        return DuelRepository(webSocketService)
    }
}
