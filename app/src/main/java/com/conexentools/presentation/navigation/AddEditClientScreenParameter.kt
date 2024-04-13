package com.conexentools.presentation.navigation

import com.conexentools.core.util.log
import com.conexentools.core.util.toClient
import com.conexentools.data.local.model.Client
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.HomeScreenViewModel
import contacts.core.entities.Contact

class AddEditClientScreenParameters {
  companion object {
    lateinit var hvm: HomeScreenViewModel
    lateinit var au: AndroidUtils
    lateinit var navigateBack: () -> Boolean
    var client: Client? = null
    var contactPickerSelectedContacts: List<Contact> = listOf()
    var indexOfLatestClientAddedFromContactPicker = 0

    var isNewClient: Boolean = false

    var onSubmitClient: (Client) -> Unit = {}
    val onAddClientFromContactPicker: (client: Client) -> Unit = { client ->
      hvm.checkIfClientIsPresentInDatabase(client, onClientNotPresent = {
        hvm.insertClient(client)
        log("Client added from contact picker. $client")
        au.toast("${client.name} añadid@")
      })

      if (indexOfLatestClientAddedFromContactPicker < contactPickerSelectedContacts.count()) {
        AddEditClientScreenParameters.client =
          contactPickerSelectedContacts[++indexOfLatestClientAddedFromContactPicker].toClient()
      } else {
        contactPickerSelectedContacts = listOf()
        indexOfLatestClientAddedFromContactPicker = 0
        navigateBack()
      }
    }
//    var onClientAddedFromContactPicker: ((Client) -> Unit)? = null

    val onAddClient: (client: Client) -> Unit = { client ->
      hvm.checkIfClientIsPresentInDatabase(client, onClientNotPresent = {
        log("Client added. $client")
        hvm.insertClient(client)
        navigateBack()
      })
    }
    val onEditClient: (client: Client) -> Unit = { client ->
      hvm.checkIfClientIsPresentInDatabase(client, onClientNotPresent = {
        log("Client edited. $client")
        hvm.updateClient(client)
        navigateBack()
      })
    }
  }
}