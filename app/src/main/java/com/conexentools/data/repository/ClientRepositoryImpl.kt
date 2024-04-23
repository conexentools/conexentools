package com.conexentools.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.conexentools.core.app.Constants
import com.conexentools.data.local.model.Client
import com.conexentools.data.local.model.ClientDao
import com.conexentools.domain.repository.ClientRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalPagingApi
class ClientRepositoryImpl @Inject constructor(
  private val clientDao: ClientDao
) : ClientRepository {
  //  override fun getAllClients(): Flow<List<Client>> {
//    return clientDao.getAllClients()
//  }
  override fun getAllClients(): Flow<PagingData<Client>> {
    val pagingSourceFactory = { clientDao.getAllClients() }
    return Pager(
      config = PagingConfig(pageSize = Constants.ITEMS_PER_PAGE),
      initialKey = null,
      pagingSourceFactory = pagingSourceFactory
    ).flow
  }

  override suspend fun insert(client: Client) {
    clientDao.insertClient(client)
  }

  override suspend fun update(client: Client) {
    clientDao.updateClient(client)
  }

  override suspend fun delete(id: Long) {
    clientDao.delete(id)
  }

  override suspend fun count(client: Client): Int {
    return clientDao.count(
      name = client.name,
      phoneNumber = client.phoneNumber,
      cardNumber = client.cardNumber,
      latestRechargeDateISOString = client.latestRechargeDateISOString,
      imageUriString = client.imageUriString,
      quickMessage = client.quickMessage,
      rechargesMade = client.rechargesMade,
    )
  }

  override suspend fun cleanDatabase() = clientDao.cleanDatabase()
}