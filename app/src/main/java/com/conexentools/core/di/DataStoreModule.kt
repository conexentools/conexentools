package com.conexentools.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.conexentools.core.app.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
  @Provides
  @Singleton
  fun provideUserPreferences(
    @ApplicationContext context: Context
  ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
//      corruptionHandler = {},
    produceFile = {
      context.preferencesDataStoreFile(name = Constants.APP_SETTINGS)
    }
  )
}