package com.conexentools.domain.use_cases.room

import com.conexentools.data.local.model.Client
import com.conexentools.domain.repository.ClientRepository
import javax.inject.Inject

class InsertClientUseCase @Inject constructor(
  private val cr: ClientRepository
) {
  suspend operator fun invoke(client: Client) = cr.insert(client)
}