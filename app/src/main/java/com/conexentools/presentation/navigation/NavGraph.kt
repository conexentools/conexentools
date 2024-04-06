package com.conexentools.presentation.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.conexentools.core.util.ObserveLifecycleEvents
import com.conexentools.core.util.log
import com.conexentools.core.util.pickContact
import com.conexentools.data.local.model.Client
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.HomeScreenViewModel
import com.conexentools.presentation.components.screens.about.AboutScreen
import com.conexentools.presentation.components.screens.add_edit_client.AddEditClientScreen
import com.conexentools.presentation.components.screens.home.HomeScreen
import com.conexentools.presentation.components.screens.settings.SettingsScreen
import contacts.core.util.phoneList
import kotlinx.coroutines.launch

@Composable
fun SetUpNavGraph(
  navController: NavHostController,
  hvm: HomeScreenViewModel = viewModel(LocalContext.current as ComponentActivity),
  startDestination: Screen = Screen.Home,
  au: AndroidUtils
) {

  hvm.ObserveLifecycleEvents(lifecycle = LocalLifecycleOwner.current.lifecycle)

  NavHost(
    navController = navController,
    startDestination = startDestination.route
  ) {

    var onEditionClient: Client? = null
    fun navigateBack() { navController.popBackStack() }

    composable(route = Screen.Home.route) {
      val pickContactLauncher = pickContact(au = au) { contact ->
        if (contact != null) {
          val number =
            if (contact.phoneList().isNotEmpty()) contact.phoneList().first().number else null
          hvm.waContact.value = number ?: contact.displayNamePrimary ?: "sin nombre :("
          hvm.waContactImageUri.value = contact.photoThumbnailUri
        }
      }

      val homeScreenState by hvm.state.collectAsState()

      val clientPagingItems = hvm.clients.collectAsLazyPagingItems()
//      val clientPagingItems = flowOf(PagingData.from(clientsForTesting)).collectAsLazyPagingItems()
      HomeScreen(
        homeScreenState = homeScreenState,
        navController = navController,
        savePreferencesAction = hvm::saveUserPreferences,
        page = hvm.initialHomeScreenPage,
        au = au,

        // InstrumentationTest Page
        onPickContactButton = {
          pickContactLauncher.launch(null)
//          contactPickerActivityLauncher.launch(pickContact)
        },
        adbInstrumentationRunCommandGetter = hvm::getAdbInstrumentationRunCommand,
        runInstrumentationTest = hvm::runInstrumentationTest,
        updateRechargeAvailability = hvm::updateRechargeAvailability,
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

        // ClientList Page
        isManager = hvm.isManager,
        clientPagingItems = clientPagingItems,
        onClientEdit = {
          log("About to edit client: $it")
          onEditionClient = it
          navController.navigate(Screen.AddEditClient)
        },
        onClientSendMessage = { number, message ->
          log("Sending message to number: $number. With message: $message")
          hvm.sendWAMessage(number, message)
        },
        onClientRecharge = hvm::rechargeClient,
        onClientDelete = { client: Client ->
          log("Client deleted: $client")
          hvm.deleteClient(clientId = client.id)
        },
        onAddClient = {
          onEditionClient = null
          navController.navigate(Screen.AddEditClient)
        },
        onClientRechargeCounterReset = {
          log("Client recharge counter restored")
          hvm.updateClient(it)
        }
      )
    }

    // Add/Edit Client Screen
    composable(route = Screen.AddEditClient.route) {

      val coroutineScope = rememberCoroutineScope()

      fun onClientAddEdit(client: Client, action: () -> Unit) {
        var clientExists = false
        coroutineScope.launch {
          log("about to count matching clients")
          val count = hvm.countClientMatches(client)
          log("count: $count")
          clientExists = count != 0
        }.invokeOnCompletion {
          if (clientExists) {
            au.toast("El cliente ya existe", vibrate = true)
          } else {
            action()
            navController.popBackStack()
          }
        }
      }

      AddEditClientScreen(
        client = onEditionClient,
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
        onCancel = ::navigateBack,
        onNavigateBack = ::navigateBack,
        au = au,
      )
    }

    // Settings Screen
    composable(route = Screen.Settings.route) {
      SettingsScreen(
        appTheme = hvm.appTheme,
        alwaysWaMessageByIntent = hvm.alwaysWaMessageByIntent,
        onNavigateBack = ::navigateBack
      )
    }

    // About Screen
    composable(route = Screen.About.route) {
      AboutScreen(
        onNavigateBack = ::navigateBack,
        au = au
      )
    }
  }
}

fun NavController.navigate(screen: Screen) {
  navigate(screen.route)
}
