package com.conexentools.presentation.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.paging.compose.collectAsLazyPagingItems
import com.conexentools.core.util.ObserveLifecycleEvents
import com.conexentools.core.util.composable
import com.conexentools.core.util.log
import com.conexentools.core.util.navigate
import com.conexentools.core.util.popUpTo
import com.conexentools.core.util.toClient
import com.conexentools.data.local.model.Client
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.HomeScreenViewModel
import com.conexentools.presentation.components.screens.about.AboutScreen
import com.conexentools.presentation.components.screens.add_edit_client.AddEditClientScreen
import com.conexentools.presentation.components.screens.contact_picker.ContactPickerScreen
import com.conexentools.presentation.components.screens.help.HelpScreen
import com.conexentools.presentation.components.screens.home.HomeScreen
import com.conexentools.presentation.components.screens.home.components.RequestAppPermissions
import com.conexentools.presentation.components.screens.settings.SettingsScreen
import contacts.core.entities.Contact


private var onEditionClient: Client? = null
private var selectedClientsFromContactPicker: List<Contact> = listOf()
private var latestIndexOfClientAddedFromContactPicker = 0

@Composable
fun SetUpNavGraph(
  navController: NavHostController,
  hvm: HomeScreenViewModel = viewModel(LocalContext.current as ComponentActivity),
  startDestination: Screen = Screen.Home,
  au: AndroidUtils
) {

  hvm.ObserveLifecycleEvents(lifecycle = LocalLifecycleOwner.current.lifecycle)
  var permissionsRequested by remember { mutableStateOf(false) }
  if (!permissionsRequested) {
    RequestAppPermissions(
      au = au,
      appLaunchCount = hvm.appLaunchCount.intValue,
      onPermissionsRequestComplete = {
        permissionsRequested = true
        hvm.initialClientsLoad()
      }
    )
  }

  NavHost(
    navController = navController,
    startDestination = startDestination.route
  ) {


    fun navigateBack() = navController.popBackStack()

//    fun onClientAddEdit(client: Client, action: () -> Unit) {
//      hvm.checkIfClientIsPresent(client, onClientNotPresent = {
//        action()
//        navController.popBackStack()
//      })
//    }
    /*
            onAdd = {
          onClientAddEdit(it) {
            log("Client added. $it")
            hvm.insertClient(it)
          }
        },
        onEdit = {
          onClientAddEdit(it) {
            log("Client edited. $it")
            hvm.updateClient(it)
          }
        },
     */
    var onAddEditClientScreen_SubmitClient: (Client) -> Unit = {}
    val onAddEditClientScreen_AddClientFromContactPicker: (client: Client) -> Unit = { client ->
      hvm.checkIfClientIsPresentInDatabase(client, onClientNotPresent = {
        hvm.insertClient(client)
        log("Client added from batch selection. $client")
        au.toast("${client.name} añadid@")
      })
    }
    val onAddEditClientScreen_ClientAddedFromContactPicker: (MutableState<Client>) -> Unit =
      { onEditionClient ->
        if (latestIndexOfClientAddedFromContactPicker != -1) {
          if (latestIndexOfClientAddedFromContactPicker < selectedClientsFromContactPicker.count()) {
            onEditionClient.value =
              selectedClientsFromContactPicker[++latestIndexOfClientAddedFromContactPicker].toClient()
          } else {
            selectedClientsFromContactPicker = listOf()
            latestIndexOfClientAddedFromContactPicker = 0
            navigateBack()
          }
        }
      }

    val onAddEditClientScreen_AddClient: (client: Client) -> Unit = { client ->
      hvm.checkIfClientIsPresentInDatabase(client, onClientNotPresent = {
        log("Client added. $client")
        hvm.insertClient(client)
        navigateBack()
      })
    }
    val onAddEditClientScreen_EditClient: (client: Client) -> Unit = { client ->
      hvm.checkIfClientIsPresentInDatabase(client, onClientNotPresent = {
        log("Client edited. $client")
        hvm.updateClient(client)
        navigateBack()
      })
    }
    composable(Screen.Home) {

      val homeScreenState by hvm.state.collectAsState()

      val clientPagingItems = hvm.clients.collectAsLazyPagingItems()

//      val clientPagingItems = flowOf(PagingData.from(clientsForTesting)).collectAsLazyPagingItems()
      HomeScreen(
        homeScreenState = homeScreenState,
        navController = navController,
//        onInitialCompositionOfClientsPage = {
//          hvm.initialClientsLoad()
//        },
        page = hvm.initialHomeScreenPage,
        au = au,

        // InstrumentationTest Page
//        onPickContactButton = {
//          pickContactLauncher.launch(null)
////          contactPickerActivityLauncher.launch(pickContact)
//        },
        adbInstrumentationRunCommandGetter = hvm::getAdbInstrumentationRunCommand,
//        runInstrumentationTest = hvm::runInstrumentedTest,
//        updateRechargeAvailability = hvm::updateRechargeAvailability,
        firstClientNumber = hvm.firstClientNumber,
        secondClientNumber = hvm.secondClientNumber,
        firstClientRecharge = hvm.firstClientRecharge,
        secondClientRecharge = hvm.secondClientRecharge,
        fetchDataFromWA = hvm.fetchDataFromWA,
        pin = hvm.pin,
        bank = hvm.bank,
        cardLast4Digits = hvm.cardLast4Digits,
        waContactImageUri = hvm.waContactImageUri,
        rechargesAvailabilityDateISOString = hvm.rechargesAvailabilityDateISOString,
        waContact = hvm.waContact,
        onRunInstrumentedTest = hvm::runInstrumentedTest,
        // ClientList Page
        isManager = hvm.isManager,
        clientPagingItems = clientPagingItems,
        onClientCardEdit = { client ->
          log("About to edit client: $client")
          onEditionClient = client
          latestIndexOfClientAddedFromContactPicker = -1
          onAddEditClientScreen_SubmitClient = { onAddEditClientScreen_EditClient(it) }
          navController.navigate(Screen.AddEditClient)
        },
        onClientCardSendMessage = { number, message ->
          log("Sending message to number: $number. With message: $message")
          hvm.sendWAMessage(number, message)
        },
        onClientCardRecharge = hvm::rechargeClient,
        onClientCardDelete = { client: Client ->
          log("Client deleted: $client")
          hvm.deleteClient(clientId = client.id)
        },
        onAddClient = {
          onEditionClient = null
          onAddEditClientScreen_SubmitClient = { onAddEditClientScreen_AddClient(it) }
          latestIndexOfClientAddedFromContactPicker = -1
          navController.navigate(Screen.AddEditClient)
        },
        onBatchAddClient = {
//          onEditionClient = null
//          onAddEditClientScreen_SubmitClient = onAddEditClientScreen_AddClientFromContactPicker
//          onAddEditClientScreen_AddClientFromContactPicker = {
//
//          }
          onAddEditClientScreen_SubmitClient = onAddEditClientScreen_AddClientFromContactPicker
          navController.navigate(Screen.ContactPicker)
        },
        onClientCardRechargeCounterReset = {
          log("Client recharge counter restored")
          hvm.updateClient(it)
        }
      )
    }

    // Add|Edit Client Screen
    composable(Screen.AddEditClient) {
      AddEditClientScreen(
        client = onEditionClient,
        onCancel = ::navigateBack,
        onNavigateBack = ::navigateBack,
        onSubmit = onAddEditClientScreen_SubmitClient,
        onClientAddedFromContactPicker = onAddEditClientScreen_ClientAddedFromContactPicker,
        au = au,
      )
    }

    // Settings Screen
    composable(Screen.Settings) {
      SettingsScreen(
        appTheme = hvm.appTheme,
        alwaysWaMessageByIntent = hvm.alwaysWaMessageByIntent,
        onNavigateBack = ::navigateBack
      )
    }

    // About Screen
    composable(Screen.About) {
      AboutScreen(
        onNavigateBack = ::navigateBack,
        au = au
      )
    }

    // Help Screen
    composable(Screen.Help) {
      HelpScreen(
        onNavigateBack = ::navigateBack,
      )
    }

    // Contact Picker Screen
    composable(Screen.ContactPicker) {
      ContactPickerScreen(
        onSelectionDone = { selectedContacts ->

          if (selectedContacts.isNotEmpty()) {
            onEditionClient = selectedContacts.first().toClient()
            latestIndexOfClientAddedFromContactPicker = 0
            selectedClientsFromContactPicker = selectedContacts
            navController.navigate(Screen.AddEditClient) {
              popUpTo(Screen.AddEditClient) {
                inclusive = true
              }
            }
          } else {
            navigateBack()
          }
//          onAddEditClientScreen_ClientAddedFromBatch = {
//
//          }

//          fun addClient(index: Int){
////            val remainingContacts = contacts.subList(index, toIndex = contacts.count() - 1)
//            val client = selectedContacts[index]
////                      onAddEditClientScreen_ClientAddedFromBatch = {
////
////          }
//            if (index < selectedContacts.count()){
//              addClient(index + 1)
//            } else {
//
//            }
//          }
//          addClient(selectedContacts)
//          onAddEditClientScreen_AddClientFromContactPicker = {
//            onAddEditClientScreen_AddClientFromContactPicker(it)
//
//          }
//          onAddEditClientScreen_SubmitClient = onAddEditClientScreen_AddClientFromContactPicker
//
//          onAddEditClientScreen_ClientAddedFromBatch = {
//
//          }

        },
        onNavigateBack = ::navigateBack,
      )
    }
  }
}
