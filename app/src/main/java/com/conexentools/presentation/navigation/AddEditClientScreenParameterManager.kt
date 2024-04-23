package com.conexentools.presentation.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.conexentools.core.util.log
import com.conexentools.core.util.toClient
import com.conexentools.data.local.model.Client
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.HomeScreenViewModel
import contacts.core.entities.Contact

class AddEditClientScreenParameterManager {
  companion object {
    lateinit var hvm: HomeScreenViewModel
    lateinit var au: AndroidUtils
    lateinit var navigateToHome: () -> Unit
    var client: MutableState<Client>? = null
    var contactPickerSelectedContacts: List<Contact> = listOf()
    var indexOfNextClientToAddFromContactPickerSelectedContacts = 0

    var isNewClient: Boolean = false

    var onSubmitClient: (Client) -> Unit = {}
    var onOmitClient: (() -> Unit)? = null

    val onAddClientFromContactPicker: (client: Client) -> Unit = { client ->
      hvm.checkIfClientIsPresentInDatabase(client, onClientNotPresent = {
        hvm.insertClient(client)
        log("Client added from contact picker. $client")
        au.toast("${client.name} añadid@", shortToast = true)
      })
      updateNextClientToProcessFromContactPicker()
    }

    val updateNextClientToProcessFromContactPicker = {
      if (indexOfNextClientToAddFromContactPickerSelectedContacts < contactPickerSelectedContacts.count()) {
        val nextClient =
          contactPickerSelectedContacts[indexOfNextClientToAddFromContactPickerSelectedContacts++].toClient()
        if (nextClient.phoneNumber != null && nextClient.phoneNumber!!.length != 8) {
          au.toast(
            "El número del contacto '${nextClient.name}' parece no ser un número cubano",
            vibrate = true
          )
          nextClient.phoneNumber = ""
        }
        if (client == null)
          client = mutableStateOf(nextClient)
        else
          client!!.value = nextClient
      } else {
        contactPickerSelectedContacts = listOf()
        indexOfNextClientToAddFromContactPickerSelectedContacts = 0
        navigateToHome()
      }
    }

    val onAddClient: (client: Client) -> Unit = { client ->
      hvm.checkIfClientIsPresentInDatabase(client, onClientNotPresent = {
        log("Client added. $client")
        hvm.insertClient(client)
        navigateToHome()
      })
    }

    val onEditClient: (client: Client) -> Unit = { client ->
      hvm.checkIfClientIsPresentInDatabase(client, onClientNotPresent = {
        log("Client edited. $client")
        hvm.updateClient(client)
        navigateToHome()
      })
    }
  }
}