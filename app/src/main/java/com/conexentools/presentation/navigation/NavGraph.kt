package com.conexentools.presentation.navigation

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
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
import com.conexentools.core.util.navigateAndPopDestinationFromTheBackStack
import com.conexentools.core.util.toClient
import com.conexentools.data.local.model.Client
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.HomeScreenViewModel
import com.conexentools.presentation.components.common.cleanCubanMobileNumber
import com.conexentools.presentation.components.screens.about.AboutScreen
import com.conexentools.presentation.components.screens.add_edit_client.AddEditClientScreen
import com.conexentools.presentation.components.screens.contact_picker.ContactPickerScreen
import com.conexentools.presentation.components.screens.help.HelpScreen
import com.conexentools.presentation.components.screens.home.HomeScreen
import com.conexentools.presentation.components.screens.home.components.RequestAppPermissions
import com.conexentools.presentation.components.screens.settings.SettingsScreen

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

    fun popBackStack() = navController.popBackStack()
    AddEditClientScreenParameterManager.hvm = hvm
    AddEditClientScreenParameterManager.au = au
    AddEditClientScreenParameterManager.navigateToHome =
      { navController.navigateAndPopDestinationFromTheBackStack(Screen.Home) }

    composable(Screen.Home) {

      val homeScreenState by hvm.state.collectAsState()
      val clientPagingItems = hvm.clients.collectAsLazyPagingItems()

      HomeScreen(
        homeScreenState = homeScreenState,
        navController = navController,
        page = hvm.initialHomeScreenPage,
        clientListPageHelpDialogShowed = hvm.clientListPageHelpDialogsShowed,
        au = au,

        // InstrumentationTest Page
        adbInstrumentationRunCommandGetter = hvm::getAdbInstrumentationRunCommand,
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
        onClientCardSendMessage = { number, message ->
          log("Sending message to number: $number. With message: $message")
          hvm.sendWAMessage(number, message)
        },
        onClientCardRecharge = hvm::rechargeClient,
        onClientCardDelete = { client: Client ->
          log("Client deleted: $client")
          hvm.deleteClient(clientId = client.id)
        },
        onClientCardEdit = {
          log("About to edit client: $it")
          with(AddEditClientScreenParameterManager) {
            client = mutableStateOf(it)
            onSubmitClient = onEditClient
            onOmitClient = null
            isNewClient = false
          }
          navController.navigateAndPopDestinationFromTheBackStack(Screen.AddEditClient)
        },
        onAddClient = {
          with(AddEditClientScreenParameterManager) {
            client = null
            onSubmitClient = onAddClient
            onOmitClient = null
            isNewClient = true
          }
          navController.navigateAndPopDestinationFromTheBackStack(Screen.AddEditClient)
        },
        onBatchAddClient = {
          if (au.isPermissionGranted(Manifest.permission.READ_CONTACTS)) {
            with(AddEditClientScreenParameterManager) {
              onSubmitClient = onAddClientFromContactPicker
              onOmitClient = updateNextClientToProcessFromContactPicker
              isNewClient = true
            }
            navController.navigateAndPopDestinationFromTheBackStack(Screen.ContactPicker)
          } else {
            au.toast("Permiso para leer contactos requerido", vibrate = true)
          }
        },
        onClientCardRechargeCounterReset = {
          log("Client recharge counter restored")
          hvm.updateClient(it)
        }
      )
    }

    // Add|Edit Client Screen
    composable(Screen.AddEditClient) {
      with(AddEditClientScreenParameterManager) {
        AddEditClientScreen(
          client = client ?: remember { mutableStateOf(Client()) },
          onCancel = { navController.navigate(Screen.Home) },
          onNavigateBack = { navController.navigate(Screen.Home) },
          onSubmit = onSubmitClient,
          onOmit = onOmitClient,
          isNewClient = isNewClient,
          au = au,
        )
      }
    }

    // Settings Screen
    composable(Screen.Settings) {
      SettingsScreen(
        appTheme = hvm.appTheme,
        alwaysWaMessageByIntent = hvm.alwaysWaMessageByIntent,
        onNavigateBack = ::popBackStack
      )
    }

    // About Screen
    composable(Screen.About) {
      AboutScreen(
        onNavigateBack = ::popBackStack,
        au = au
      )
    }

    // Help Screen
    composable(Screen.Help) {
      HelpScreen(
        onNavigateBack = ::popBackStack,
      )
    }

    // Contact Picker Screen
    composable(Screen.ContactPicker) {
      ContactPickerScreen(
        onSelectionDone = { selectedContacts ->
          if (selectedContacts.isEmpty()) {
            navController.navigateAndPopDestinationFromTheBackStack(Screen.Home)
          } else {
            val filteredContacts = selectedContacts.filter { contact ->
              val client = contact.toClient()

              if (client.phoneNumber.isNullOrEmpty())
                true
              else {
                val isCubanNumber: Boolean =
                  client.phoneNumber!!.cleanCubanMobileNumber().length == 8
                if (!isCubanNumber)
                  au.toast("El contacto '${client.name}' fue omitido porque no parece tener un número cubano")
                isCubanNumber
              }
            }

            AddEditClientScreenParameterManager.client =
              mutableStateOf(filteredContacts.first().toClient())
            AddEditClientScreenParameterManager.indexOfNextClientToAddFromContactPickerSelectedContacts =
              1
            AddEditClientScreenParameterManager.contactPickerSelectedContacts = filteredContacts
            navController.navigateAndPopDestinationFromTheBackStack(Screen.AddEditClient)
          }
        },
        multiContactSelectionOnly = true,
        onNavigateBack = ::popBackStack,
        au = au,
      )
    }
  }
}
