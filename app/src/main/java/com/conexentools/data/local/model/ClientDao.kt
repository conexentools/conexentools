package com.conexentools.data.local.model

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ClientDao {
  @Query("SELECT * FROM client")
  fun getAllClients(): PagingSource<Int, Client>
//  fun getAllClients() : Flow<List<Client>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertClient(client: Client)

  @Update(onConflict = OnConflictStrategy.REPLACE)
  suspend fun updateClient(client: Client)

  @Query("DELETE FROM client WHERE id=:id")
  suspend fun delete(id: Long)

  /*
    var name: String = "",
  var phoneNumber: String? = null,
  var cardNumber: String? = null,
  var latestRechargeDateISOString: String? = null,
  var imageUriString: String? = null,
  var quickMessage: String? = null,
  var rechargesMade: Int? = 0,
   */

  @Query("SELECT COUNT() FROM client WHERE name=:name AND phoneNumber is :phoneNumber AND cardNumber is :cardNumber AND latestRechargeDateISOString is :latestRechargeDateISOString AND imageUriString is :imageUriString AND quickMessage is :quickMessage AND rechargesMade is :rechargesMade")
  suspend fun count(
    name: String,
    phoneNumber: String?,
    cardNumber: String?,
    latestRechargeDateISOString: String?,
    imageUriString: String?,
    quickMessage: String?,
    rechargesMade: Int?,
  ): Int
}