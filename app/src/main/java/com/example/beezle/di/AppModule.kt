package com.example.beezle.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.beezle.data.local.LocalDataStoreRepository
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
//    @Provides
//    @Singleton
//    fun provideBookmarkDao(@ApplicationContext context: Context): BookmarkDao {
//        return MainDatabase.getDatabase(context).bookmarkDao()
//    }

    @Provides
    @Singleton
    fun provideLocalDatastoreRepository(@ApplicationContext context: Context): LocalDataStoreRepository {
        return LocalDataStoreRepository(context.dataStore)
    }
}