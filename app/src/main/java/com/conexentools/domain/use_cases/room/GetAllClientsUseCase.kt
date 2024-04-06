package com.conexentools.domain.use_cases.room

import com.conexentools.domain.repository.ClientRepository
import javax.inject.Inject

class GetAllClientsUseCase @Inject constructor(
  private val cr: ClientRepository
) {
  operator fun invoke() = cr.getAllClients()
}