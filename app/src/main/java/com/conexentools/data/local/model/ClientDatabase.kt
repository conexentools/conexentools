package com.conexentools.data.local.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
  entities = [Client::class],
  version = 2,
  exportSchema = false
)
abstract class ClientDatabase : RoomDatabase() {
  abstract val clientDao: ClientDao
}