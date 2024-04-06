package com.conexentools.domain.use_cases.room

import com.conexentools.domain.repository.ClientRepository
import javax.inject.Inject

class DeleteClientUseCase @Inject constructor(
  private val cr: ClientRepository
) {
  suspend operator fun invoke(clientId: Long) = cr.delete(clientId)
}