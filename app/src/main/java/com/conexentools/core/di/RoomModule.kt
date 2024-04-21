package com.conexentools.core.di

import android.content.Context
import android.os.Environment
import androidx.paging.ExperimentalPagingApi
import androidx.room.Room
import com.conexentools.R
import com.conexentools.core.app.Constants
import com.conexentools.core.util.log
import com.conexentools.data.local.model.ClientDao
import com.conexentools.data.local.model.ClientDatabase
import com.conexentools.data.repository.ClientRepositoryImpl
import com.conexentools.domain.repository.AndroidUtils
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
//  @Singleton
  fun provideClientDatabase(
    @ApplicationContext context: Context,
    au: AndroidUtils
  ): ClientDatabase {

    log("Creating database")
    val appName = context.resources.getString(R.string.app_name)
    val isExternalStorageWritable: Boolean =
      Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    val databasePath = if (isExternalStorageWritable) {
      // Trying to locate database on external storage shared folder
      if (au.hasExternalStorageReadWriteAccess()) {
        val externalStorage = Environment.getExternalStorageDirectory()
        log("Locating database in external storage shared folder")
//      val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val databasePath = File(externalStorage, "$appName/database")
        if (!databasePath.exists()) {
          databasePath.mkdirs()
        }
        databasePath.toString() + "/" + Constants.DATABASE_NAME
      } else {
        // Falling back to app private external storage /storage/emulated/0/Android/data/com.conexentools/database
        val externalFilesDir = context.getExternalFilesDir("")
        if (externalFilesDir != null) {
          log("Locating database on app private external storage")
          File(externalFilesDir.parent, "database/${Constants.DATABASE_NAME}").toString()
        } else {
          // Falling back to app private internal storage ~/data/data/com.conexentools/database
          log("App External files directory couldn't be retrieved. Locating database on app private internal storage")
          Constants.DATABASE_NAME
        }
      }
    } else {
      // Falling back to app private internal storage ~/data/data/com.conexentools/database
      log("database located on app private internal storage")
      Constants.DATABASE_NAME
    }

    log("Database Path: '$databasePath'")

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