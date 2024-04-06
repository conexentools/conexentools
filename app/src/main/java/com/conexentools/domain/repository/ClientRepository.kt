package com.conexentools.domain.repository

import androidx.paging.PagingData
import com.conexentools.data.local.model.Client
import kotlinx.coroutines.flow.Flow

interface ClientRepository {
//  fun getAllClients() : Flow<List<Client>>
  fun getAllClients() : Flow<PagingData<Client>>
  suspend fun insert(client: Client)
  suspend fun update(client: Client)
  suspend fun delete(id: Long)
  suspend fun count(client: Client): Int
}