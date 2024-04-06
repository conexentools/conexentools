package com.conexentools.core.di

import com.conexentools.data.repository.AndroidUtilsImpl
import com.conexentools.data.repository.UserPreferencesImpl
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.domain.repository.UserPreferences
import com.conexentools.core.util.CoroutinesDispatchers
import com.conexentools.core.util.CoroutinesDispatchersImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

  @Binds
  @Singleton
  fun bindUserPreferences(
    userPreferences: UserPreferencesImpl
  ): UserPreferences

  @Binds
  @Singleton
  fun bindDispatchers(
    dispatchers: CoroutinesDispatchersImpl
  ): CoroutinesDispatchers

  @Binds
  @Singleton
  fun bindAndroidUtils(
    androidUtils: AndroidUtilsImpl
  ): AndroidUtils

}