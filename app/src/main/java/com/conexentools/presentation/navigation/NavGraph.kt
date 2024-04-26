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

  // Client paging items MUST be collected here, before NavHost, in order to LazyColumn remember its scroll position
  // https://issuetracker.google.com/issues/177245496
  val homeScreenClientsListLazyPagingItems = hvm.clients.collectAsLazyPagingItems()

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

      HomeScreen(
        homeScreenState = homeScreenState,
        navController = navController,
        page = hvm.initialHomeScreenPage,
        clientListPageHelpDialogsShowed = hvm.clientListPageHelpDialogsShowed,
        au = au,

        // InstrumentationTest Page
        rechargeMobileInstrumentedTestAdbCommandGetter = { "adb shell " + hvm.getShellCommandToRunRechargeMobileInstrumentedTest() },
        firstClientNumber = hvm.firstClientNumber,
        secondClientNumber = hvm.secondClientNumber,
        firstClientRecharge = hvm.firstClientRecharge,
        secondClientRecharge = hvm.secondClientRecharge,
        fetchDataFromWA = hvm.fetchDataFromWA,
        pin = hvm.pin,
        bank = hvm.bank,
        cardToUseDropDownMenuPosition = hvm.cardToUseDropDownMenuPosition,
        waContactImageUri = hvm.waContactImageUri,
        rechargesAvailabilityDateISOString = hvm.rechargesAvailabilityDateISOString,
        waContact = hvm.waContact,
        onRunInstrumentedTest = hvm::runRechargeMobileInstrumentedTest,
        whatsAppInstalledVersion = hvm.whatsAppInstalledVersion,
        transfermovilInstalledVersion = hvm.transfermovilInstalledVersion,
        instrumentationAppInstalledVersion = hvm.instrumentationAppInstalledVersion,

        // ClientList Page
        isManager = hvm.isManager,
        clients = homeScreenClientsListLazyPagingItems,
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
        onSubmitClientForDeletion = { client ->
          client.submittedForDeletionFlag = 1
          hvm.updateClient(client)
        },
        onDeleteClient = { client: Client ->
          log("Client deleted: $client")
          hvm.deleteClient(clientId = client.id)
        },
        onClientRestoredFromDeletion = { client ->
          client.submittedForDeletionFlag = 0
          hvm.updateClient(client)
        },
        onClientCardSendMessage = { number, message ->
          log("Sending message to number: $number. With message: $message")
          hvm.sendWhatsAppMessage(number, message)
        },
        onClientCardCounterReset = hvm::updateClient,
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
        clientsListScrollPosition = hvm.homeScreenClientListScrollPosition,
        onTransferCashToClient = hvm::transferCash,
        defaultMobileToSendCashTransferConfirmation = hvm.defaultMobileToSendCashTransferConfirmation.value,
        transferCashInstrumentedTestAdbCommandGetter = { recipientCard: String, mobileToConfirm: String ->
          "adb shell " + hvm.getShellCommandToRunTransferCashInstrumentedTest(recipientCard, mobileToConfirm)
        },
        onRunTransferCashInstrumentedTest = hvm::runTransferCashInstrumentedTest,
        recipientReceiveMyMobileNumberAfterCashTransfer = hvm.recipientReceiveMyMobileNumberAfterCashTransfer,
        cashToTransferToClient = hvm.cashToTransfer,
        sendWhatsAppMessageOnTransferCashTestCompleted = hvm.sendWhatsAppMessageOnTransferCashTestCompleted,
        whatsAppMessageToSendOnTransferCashTestCompleted = hvm.whatsAppMessageToSendOnTransferCashTestCompleted,
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
        savePin = hvm.savePin,
        joinMessages = hvm.joinMessages,
        defaultMobileToSendCashTransferConfirmation = hvm.defaultMobileToSendCashTransferConfirmation,
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
            AddEditClientScreenParameterManager.contactPickerSelectedContacts = selectedContacts
            AddEditClientScreenParameterManager.indexOfNextClientToAddFromContactPickerSelectedContacts = 0
            AddEditClientScreenParameterManager.updateNextClientToProcessFromContactPicker()
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
