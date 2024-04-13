package com.conexentools.presentation.components.screens.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.conexentools.core.app.Constants
import com.conexentools.core.util.PreviewComposable
import com.conexentools.core.util.navigate
import com.conexentools.core.util.textFilter
import com.conexentools.core.util.truncate
import com.conexentools.data.local.model.Client
import com.conexentools.data.repository.AndroidUtilsImpl
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.screens.home.components.HomeScreenFAB
import com.conexentools.presentation.components.screens.home.components.HomeTopBar
import com.conexentools.presentation.components.screens.home.enums.HomeScreenPage
import com.conexentools.presentation.components.screens.home.pages.client_list.ClientsListPage
import com.conexentools.presentation.components.screens.home.pages.client_list.clientsForTesting
import com.conexentools.presentation.components.screens.home.pages.instrumented_test.InstrumentedTestPage
import com.conexentools.presentation.components.screens.home.state.HomeScreenLoadingState
import com.conexentools.presentation.components.screens.home.state.HomeScreenState
import com.conexentools.presentation.navigation.Screen
import com.conexentools.presentation.theme.LocalTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
  homeScreenState: HomeScreenState,
  navController: NavController,
  page: MutableState<HomeScreenPage>,
  au: AndroidUtils,

  // InstrumentationTest Page
//  onPickContactButton: () -> Unit,
  adbInstrumentationRunCommandGetter: () -> String,
//  updateRechargeAvailability: () -> Unit,
  firstClientNumber: MutableState<String?>,
  secondClientNumber: MutableState<String?>,
  firstClientRecharge: MutableState<String?>,
  secondClientRecharge: MutableState<String?>,
  fetchDataFromWA: MutableState<Boolean>,
  pin: MutableState<String>,
  bank: MutableState<String>,
  cardLast4Digits: MutableState<String>,
  waContactImageUri: MutableState<Uri?>,
  rechargesAvailabilityDateISOString: MutableState<String?>,
  waContact: MutableState<String>,
  onRunInstrumentedTest: () -> Unit,

  // ClientList Page
  isManager: MutableState<Boolean>,
  clientPagingItems: LazyPagingItems<Client>,
  onClientCardEdit: (Client) -> Unit,
  onClientCardRecharge: (Client) -> Unit,
  onClientCardDelete: (Client) -> Unit,
  onClientCardSendMessage: (String, String?) -> Unit,
  onAddClient: () -> Unit,
  onBatchAddClient: () -> Unit,
  onClientCardRechargeCounterReset: (Client) -> Unit,
) {

  when (homeScreenState.state) {
    is HomeScreenLoadingState.ScreenLoading -> {
      Row(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        CircularProgressIndicator()
      }
    }

    is HomeScreenLoadingState.Success -> {

      DrawHome(
        navController = navController,
        page = page,
        au = au,

//        onPickContactButton = onPickContactButton,
        adbInstrumentationRunCommandGetter = adbInstrumentationRunCommandGetter,
//        updateRechargeAvailability = updateRechargeAvailability,
        firstClientNumber = firstClientNumber,
        secondClientNumber = secondClientNumber,
        firstClientRecharge = firstClientRecharge,
        secondClientRecharge = secondClientRecharge,
        fetchDataFromWA = fetchDataFromWA,
        pin = pin,
        bank = bank,
        cardLast4Digits = cardLast4Digits,
        waContactImageUri = waContactImageUri,
        rechargesAvailabilityDateISOString = rechargesAvailabilityDateISOString,
        waContact = waContact,
        onRunInstrumentedTest = onRunInstrumentedTest,

        isManager = isManager,
        clientPagingItems = clientPagingItems,
        onClientCardEdit = onClientCardEdit,
        onClientCardRecharge = onClientCardRecharge,
        onClientCardDelete = onClientCardDelete,
        onClientCardSendMessage = onClientCardSendMessage,
        onAddClient = onAddClient,
        onBatchAddClient = onBatchAddClient,
        onClientCardRechargeCounterReset = onClientCardRechargeCounterReset
      )
    }

    is HomeScreenLoadingState.Error -> {
      Column(
        modifier = Modifier.padding(Constants.Dimens.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Un error inesperado ocurrió ☹️",
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.titleLarge,
//        fontWeight = MaterialTheme.typography.titleMedium,
//        fontStyle = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = homeScreenState.state.message ?: "Unknown",
          textAlign = TextAlign.Center,
//        fontWeight = MaterialTheme.typography.titleMedium,
//        fontStyle = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("PrivateResource")
@Composable
private fun DrawHome(
  navController: NavController,
  page: MutableState<HomeScreenPage>,
  au: AndroidUtils,

  // InstrumentationTest Page
//  onPickContactButton: () -> Unit,
  adbInstrumentationRunCommandGetter: () -> String,
//  runInstrumentationTest: () -> Unit,
//  updateRechargeAvailability: () -> Unit,
  firstClientNumber: MutableState<String?>,
  secondClientNumber: MutableState<String?>,
  firstClientRecharge: MutableState<String?>,
  secondClientRecharge: MutableState<String?>,
  fetchDataFromWA: MutableState<Boolean>,
  pin: MutableState<String>,
  bank: MutableState<String>,
  cardLast4Digits: MutableState<String>,
  waContactImageUri: MutableState<Uri?>,
  rechargesAvailabilityDateISOString: MutableState<String?>,
  waContact: MutableState<String>,
  onRunInstrumentedTest: () -> Unit,

  // ClientList Page
  isManager: MutableState<Boolean>,
//  onSearchBarValueChange: (String) -> Unit,
  clientPagingItems: LazyPagingItems<Client>,
//  searchBarText: String = "",
  onClientCardEdit: (Client) -> Unit,
  onClientCardRecharge: (Client) -> Unit,
  onClientCardSendMessage: (String, String?) -> Unit,
  onClientCardDelete: (Client) -> Unit,
  onAddClient: () -> Unit,
  onBatchAddClient: () -> Unit,
  onClientCardRechargeCounterReset: (Client) -> Unit,
) {

  val showAdbRunCommandDialog = remember { mutableStateOf(false) }
  val searchBarText = remember { mutableStateOf("") }

  // ADB Command Dialog
  if (showAdbRunCommandDialog.value) {
    AlertDialog(
      onDismissRequest = {
        showAdbRunCommandDialog.value = false
      },
      title = {
        Text(text = "ADB Instrumentation Run Command")
      },
      text = {
        Text(
          text = adbInstrumentationRunCommandGetter(),
          modifier = Modifier.clickable {
            au.setClipboard(
              adbInstrumentationRunCommandGetter()
            )
            au.toast("Comando copiado al portapapeles")
          })
      },
      confirmButton = {
        TextButton(
          onClick = {
            showAdbRunCommandDialog.value = false
          }) {
          Text("Cerrar")
        }
      },
      dismissButton = {
        Row {
          TextButton(
            onClick = {
              au.openBrowser(Constants.LOCAL_ADB_ARTICLE_URL)
              showAdbRunCommandDialog.value = false
            }
          ) {
            Text("ADB local?")
          }

          TextButton(
            onClick = {
              au.openBrowser(Constants.ADB_DOWNLOAD_PAGE_URL)
              showAdbRunCommandDialog.value = false
            }
          ) {
            Text("Descargar ADB")
          }
        }
      },
    )
  }

  val pagerState = rememberPagerState(
    initialPage = page.value.ordinal,
    pageCount = { HomeScreenPage.entries.count() }
  )

  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

  Scaffold(
    topBar = {
      HomeTopBar(
        page = page,
        searchBarText = searchBarText,
        onPageChange = { page ->
          coroutineScope.launch {
            pagerState.animateScrollToPage(page.ordinal)
          }
        },
        onSettings = { navController.navigate(Screen.Settings) },
        onAbout = { navController.navigate(Screen.About) },
        onHelp = { navController.navigate(Screen.Help) },
        au = au
      )
    },
    snackbarHost = {
      SnackbarHost(
        hostState = snackbarHostState,
      ) {
        Snackbar(
          snackbarData = it,
          containerColor = MaterialTheme.colorScheme.onBackground,
          contentColor = MaterialTheme.colorScheme.background,
          actionColor = MaterialTheme.colorScheme.onPrimary
        )
      }
    },
    floatingActionButton = {
      HomeScreenFAB(
//        runInstrumentationTest = runInstrumentationTest,
//        updateRechargeAvailability = updateRechargeAvailability,
//        savePreferencesAction = savePreferencesAction,
        firstClientNumber = firstClientNumber,
        secondClientNumber = secondClientNumber,
        firstClientRecharge = firstClientRecharge,
        secondClientRecharge = secondClientRecharge,
        fetchDataFromWA = fetchDataFromWA,
        rechargesAvailabilityDateISOString = rechargesAvailabilityDateISOString,
        page = page,
        showAdbRunCommandDialog = showAdbRunCommandDialog,
        onAddClient = onAddClient,
        onRunInstrumentedTest = onRunInstrumentedTest,
        onBatchAddClient = onBatchAddClient,
      )
    },
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
  ) { paddingValues ->

    HorizontalPager(
      state = pagerState,
      verticalAlignment = Alignment.CenterVertically,
    ) { currentPageIndex ->

      var showManagerPasswordDialog by remember { mutableStateOf(false) }


      if (showManagerPasswordDialog) {

        Dialog(
          onDismissRequest = {
            showManagerPasswordDialog = false
            coroutineScope.launch {
              pagerState.scrollToPage(HomeScreenPage.INSTRUMENTED_TEST.ordinal)
            }
          }) {

          val textFieldFocusRequester = remember { FocusRequester() }

          Column(
            horizontalAlignment = Alignment.CenterHorizontally
          ) {

            var password by remember {
              mutableStateOf("")
            }

            Text(
              text = "Contraseña",
              style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(Constants.Dimens.Medium))

            val dark = LocalTheme.current.isDark
            TextField(
//              placeholder = {Text("")},
              value = password,
              singleLine = true,
              onValueChange = {
                if (it.length < 13) {
                  password = it
                  if (password == "589058825519") {
                    showManagerPasswordDialog = false
                    isManager.value = true
                    coroutineScope.launch {
                      pagerState.scrollToPage(HomeScreenPage.CLIENT_LIST.ordinal)
                    }
                  }
                }
              },
              modifier = Modifier.focusRequester(textFieldFocusRequester),
              visualTransformation = {
                textFilter(
                  text = it,
                  mask = "___0___2___9",
                  darkTheme = dark,
                )
              }
            )
          }

          var dialogWindowFocusChangeListenerRegistered by remember { mutableStateOf(false) }
          if (!dialogWindowFocusChangeListenerRegistered) {
            LocalView.current.viewTreeObserver.addOnWindowFocusChangeListener { hasFocus ->
              if (hasFocus) textFieldFocusRequester.requestFocus()
            }
            dialogWindowFocusChangeListenerRegistered = true
          }
        }
      }

      LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.distinctUntilChanged().collect {
          page.value = HomeScreenPage.fromOrdinal(it)
          if (page.value == HomeScreenPage.CLIENT_LIST && !isManager.value) {
            coroutineScope.launch {
              pagerState.scrollToPage(HomeScreenPage.INSTRUMENTED_TEST.ordinal)
            }.invokeOnCompletion { showManagerPasswordDialog = true }
          }
        }
      }

      when (HomeScreenPage.fromOrdinal(currentPageIndex)) {
        HomeScreenPage.INSTRUMENTED_TEST -> {
          InstrumentedTestPage(
            paddingValues = paddingValues,
            au = au,
            firstClientNumber = firstClientNumber,
            secondClientNumber = secondClientNumber,
            firstClientRecharge = firstClientRecharge,
            secondClientRecharge = secondClientRecharge,
            fetchDataFromWA = fetchDataFromWA,
            pin = pin,
            bank = bank,
            cardLast4Digits = cardLast4Digits,
            waContactImageUri = waContactImageUri,
            rechargesAvailabilityDateISOString = rechargesAvailabilityDateISOString,
            waContact = waContact,
          )
        }

        HomeScreenPage.CLIENT_LIST -> {
          if (isManager.value) {
            ClientsListPage(
              clientPagingItems = clientPagingItems,
              searchBarText = searchBarText.value,
              onClientEdit = onClientCardEdit,
              onClientRecharge = onClientCardRecharge,
              onClientSendMessage = onClientCardSendMessage,
              onClientDelete = { client, onClientDeleteDismissed ->
                coroutineScope.launch {
                  val snackbarResult = snackbarHostState.showSnackbar(
                    message = "${client.name.truncate(20)} eliminado",
                    actionLabel = "Deshacer",
                    duration = SnackbarDuration.Short,
                  )
                  when (snackbarResult) {
                    SnackbarResult.Dismissed -> {
                      onClientCardDelete(client)
                    }
//                  SnackbarResult.ActionPerformed -> {
//                    showClient.value = true
//                  }
                    else -> {
                      onClientDeleteDismissed()
                    }
                  }
                }
              },
              paddingValues = paddingValues,
              onClientRechargeCounterReset = onClientCardRechargeCounterReset,
              au = au
            )
          }
        }
      }
    }
  }
}

@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, apiLevel = 33, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
  val context = LocalContext.current
  PreviewComposable {
    val clientPagingItems = flowOf(PagingData.from(clientsForTesting)).collectAsLazyPagingItems()

    HomeScreen(
      homeScreenState = HomeScreenState(HomeScreenLoadingState.Success),
//      homeScreenState = HomeScreenState(HomeScreenLoadingState.Error("Some error occurred")),
      navController = rememberNavController(),
//      onPickContactButton = {},
      adbInstrumentationRunCommandGetter = { "" },
//      runInstrumentationTest = {},
//      updateRechargeAvailability = {},
      firstClientNumber = remember { mutableStateOf("55797140") },
      secondClientNumber = remember { mutableStateOf("58469745") },
//      secondClientNumber = remember { mutableStateOf(null) },
      firstClientRecharge = remember { mutableStateOf("1234") },
      secondClientRecharge = remember { mutableStateOf("2500") },
//      secondClientRecharge = remember { mutableStateOf(null) },
      fetchDataFromWA = remember { mutableStateOf(false) },
      isManager = remember { mutableStateOf(false) },
      pin = remember { mutableStateOf("5555") },
      bank = remember { mutableStateOf("Metropolitano") },
      cardLast4Digits = remember { mutableStateOf("") },
      waContactImageUri = remember { mutableStateOf(null) },
      rechargesAvailabilityDateISOString = remember { mutableStateOf(null) },
      waContact = remember { mutableStateOf("Jeans MR") },
      page = remember { mutableStateOf(HomeScreenPage.CLIENT_LIST) },
      au = AndroidUtilsImpl(context = context),
      clientPagingItems = clientPagingItems,
      onClientCardEdit = {},
      onClientCardRecharge = {},
      onClientCardDelete = {},
      onClientCardSendMessage = { _, _ -> },
      onClientCardRechargeCounterReset = {},
      onAddClient = {},
      onBatchAddClient = {},
      onRunInstrumentedTest = {}
    )
  }
}
