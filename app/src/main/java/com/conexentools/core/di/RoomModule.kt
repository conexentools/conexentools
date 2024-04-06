package com.conexentools.core.di

import android.content.Context
import android.os.Environment
import androidx.compose.ui.res.stringResource
import androidx.paging.ExperimentalPagingApi
import androidx.room.Room
import com.conexentools.R
import com.conexentools.core.app.Constants
import com.conexentools.core.util.log
import com.conexentools.data.local.model.ClientDao
import com.conexentools.data.local.model.ClientDatabase
import com.conexentools.data.repository.ClientRepositoryImpl
import com.conexentools.domain.repository.ClientRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

  @Provides
  @Singleton
  fun provideClientDatabase(
    @ApplicationContext context: Context
  ): ClientDatabase {

    var appName = context.resources.getString(R.string.app_name)
    val isExternalStorageWritable: Boolean =
      Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    val databasePath = if (isExternalStorageWritable) {
      log("Saving database in external storage")
      val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
      val databasePath = File(downloadsDirectory, appName)
//      val externalStorageDir = Environment.getExternalStorageDirectory()
//      val externalStorageDir = context.getExternalFilesDir("database")!!
//      log("externalStorageDir: '$downloadsDirectory'")
      if (!databasePath.exists()) {
        databasePath.mkdirs()
      }
      databasePath.toString() + "/" + Constants.DATABASE_NAME
    } else {
      log("Saving database on internal storage")
      Constants.DATABASE_NAME
    }
    log("Database Path: $databasePath")

    return Room.databaseBuilder(
      context = context,
      klass = ClientDatabase::class.java,
      name = databasePath
    )
//      .fallbackToDestructiveMigration()
      .build()
  }

  @Provides
  @Singleton
  fun provideClientDao(
    clientDatabase: ClientDatabase
  ): ClientDao = clientDatabase.clientDao

  @OptIn(ExperimentalPagingApi::class)
  @Provides
  @Singleton
  fun provideClientRepository(
    clientDao: ClientDao
  ): ClientRepository = ClientRepositoryImpl(clientDao)
}