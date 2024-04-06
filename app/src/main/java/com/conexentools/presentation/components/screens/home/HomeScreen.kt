package com.conexentools.presentation.components.screens.home

//import com.conexentools.data.model.MainViewModelFactory
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import com.conexentools.core.app.Constants
import com.conexentools.core.util.PreviewComposable
import com.conexentools.core.util.textFilter
import com.conexentools.core.util.truncate
import com.conexentools.data.local.model.Client
import com.conexentools.data.repository.AndroidUtilsImpl
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.screens.home.enums.HomeScreenPage
import com.conexentools.presentation.components.screens.home.pages.client_list.ClientsListPage
import com.conexentools.presentation.components.screens.home.pages.client_list.clientsForTesting
import com.conexentools.presentation.components.screens.home.pages.instrumented_test.InstrumentedTestPage
import com.conexentools.presentation.components.screens.home.state.HomeScreenLoadingState
import com.conexentools.presentation.components.screens.home.state.HomeScreenState
import com.conexentools.presentation.navigation.Screen
import com.conexentools.presentation.navigation.navigate
import com.conexentools.presentation.theme.LocalTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

//@Inject
//lateinit var au: AndroidUtils

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
  homeScreenState: HomeScreenState,
  navController: NavController,
  savePreferencesAction: () -> Unit,
  page: MutableState<HomeScreenPage>,
  au: AndroidUtils,

  // InstrumentationTest Page
  onPickContactButton: () -> Unit,
  adbInstrumentationRunCommandGetter: () -> String,
  runInstrumentationTest: () -> Unit,
  updateRechargeAvailability: () -> Unit,
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

  // ClientList Page
  isManager: MutableState<Boolean>,
  clientPagingItems: LazyPagingItems<Client>,
  onClientEdit: (Client) -> Unit,
  onClientRecharge: (Client) -> Unit,
  onClientDelete: (Client) -> Unit,
  onClientSendMessage: (String, String?) -> Unit,
  onAddClient: () -> Unit,
  onClientRechargeCounterReset: (Client) -> Unit,
) {


//  @Inject
//  val homeViewModel: HomeViewModel = viewModel()

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

//      val interactionSource = remember {
//        MutableInteractionSource()
//      }
//
//      val isDragged = interactionSource.collectIsDraggedAsState().value

//      if (isDragged){
//        navController.navigate(route = Screen.ClientsList.route)
//        Log.i("<<<CONEXEN>>>", "Navigating to ClientsListScreen")
//      }

      DrawHome(
        navController = navController,
        page = page,
        au = au,
        savePreferencesAction = savePreferencesAction,

        onPickContactButton = onPickContactButton,
        adbInstrumentationRunCommandGetter = adbInstrumentationRunCommandGetter,
        runInstrumentationTest = runInstrumentationTest,
        updateRechargeAvailability = updateRechargeAvailability,
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

        isManager = isManager,
        clientPagingItems = clientPagingItems,
        onClientEdit = onClientEdit,
        onClientRecharge = onClientRecharge,
        onClientDelete = onClientDelete,
        onClientSendMessage = onClientSendMessage,
        onAddClient = onAddClient,
        onClientRechargeCounterReset = onClientRechargeCounterReset
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
  savePreferencesAction: () -> Unit,
  page: MutableState<HomeScreenPage>,
  au: AndroidUtils,

  // InstrumentationTest Page
  onPickContactButton: () -> Unit,
  adbInstrumentationRunCommandGetter: () -> String,
  runInstrumentationTest: () -> Unit,
  updateRechargeAvailability: () -> Unit,
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

  // ClientList Page
  isManager: MutableState<Boolean>,
//  onSearchBarValueChange: (String) -> Unit,
  clientPagingItems: LazyPagingItems<Client>,
//  searchBarText: String = "",
  onClientEdit: (Client) -> Unit,
  onClientRecharge: (Client) -> Unit,
  onClientSendMessage: (String, String?) -> Unit,
  onClientDelete: (Client) -> Unit,
  onAddClient: () -> Unit,
  onClientRechargeCounterReset: (Client) -> Unit,
) {

  val showAdbRunCommandDialog = remember { mutableStateOf(false) }
  val maxPinLength = remember { mutableIntStateOf(if (bank.value == "Metropolitano") 4 else 5) }
  var searchBarText by remember { mutableStateOf("") }

//    val aa by mainViewModel.LoadData(FETCH_DATA_FROM_WA, true).collectAsState(coroutineScope.coroutineContext)
//    mainViewModel.clientsNumbers.add(mainViewModel.LoadData(FIRST_CLIENT_NUMBER_KEY, "123").collectAsState(coroutineScope.coroutineContext).value as String)
//    mainViewModel.clientsRecharges.add(mainViewModel.LoadData(FIRST_CLIENT_RECHARGE_KEY, "888").collectAsState(coroutineScope.coroutineContext).value as String)

  // ADB Command Dialog
  if (showAdbRunCommandDialog.value) {
    AlertDialog(
      onDismissRequest = {
        // Dismiss the dialog when the user clicks outside the dialog or on the back
        // button. If you want to disable that functionality, simply use an empty
        // onCloseRequest.
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
            }) {
            Text("ADB local?")
          }

          TextButton(
            onClick = {
              au.openBrowser(Constants.ADB_DOWNLOAD_PAGE_URL)
              showAdbRunCommandDialog.value = false
            }) {
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

  Scaffold(
    topBar = {
//             TopAppBar(title = { Text("WWW") })
      HomeTopBar(
        page = page,
        onSearchBarTextChange = { searchBarText = it },
        onPageChange = { page ->
          coroutineScope.launch {
            pagerState.animateScrollToPage(page.ordinal)
          }
        },
        onSettings = { navController.navigate(Screen.Settings) },
        onAbout = { navController.navigate(Screen.About) },
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
        runInstrumentationTest = runInstrumentationTest,
        updateRechargeAvailability = updateRechargeAvailability,
        savePreferencesAction = savePreferencesAction,
        firstClientNumber = firstClientNumber,
        secondClientNumber = secondClientNumber,
        firstClientRecharge = firstClientRecharge,
        secondClientRecharge = secondClientRecharge,
        fetchDataFromWA = fetchDataFromWA,
        pin = pin,
        rechargesAvailabilityDateISOString = rechargesAvailabilityDateISOString,
        page = page,
        showAdbRunCommandDialog = showAdbRunCommandDialog,
        maxPinLength = maxPinLength,
        onAddClient = onAddClient,
        au = au
      )
    }
  ) { paddingValues ->

    HorizontalPager(
      state = pagerState,
      verticalAlignment = Alignment.CenterVertically,
    ) { currentPageIndex ->

      var showManagerPasswordDialog by remember { mutableStateOf(false) }

      if (showManagerPasswordDialog) {
        val focusRequester = remember { FocusRequester() }
        Dialog(
          onDismissRequest = {
            showManagerPasswordDialog = false
            coroutineScope.launch {
              pagerState.scrollToPage(HomeScreenPage.INSTRUMENTED_TEST.ordinal)
            }
          }) {
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
              modifier = Modifier.focusRequester(focusRequester),
              visualTransformation = {
                textFilter(
                  text = it,
                  mask = "___0___2___9",
                  darkTheme = dark,
                )
              }
            )
          }
        }

        LaunchedEffect(true){
          focusRequester.requestFocus()
        }
      }

      LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.distinctUntilChanged().collect {
//          if (isManager.value || it == HomeScreenPage.INSTRUMENTED_TEST.ordinal)
          page.value = HomeScreenPage.fromOrdinal(it)
          if (page.value == HomeScreenPage.CLIENT_LIST && !isManager.value) {
            coroutineScope.launch {
              pagerState.scrollToPage(HomeScreenPage.INSTRUMENTED_TEST.ordinal)
            }.invokeOnCompletion { showManagerPasswordDialog = true }
          }
        }
      }

      when (currentPageIndex) {
        HomeScreenPage.INSTRUMENTED_TEST.ordinal -> {
          InstrumentedTestPage(
            paddingValues = paddingValues,
            onPickContact = onPickContactButton,
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
            maxPinLength = maxPinLength
          )
        }

        HomeScreenPage.CLIENT_LIST.ordinal -> {
          if (isManager.value) {
            ClientsListPage(
              clientPagingItems = clientPagingItems,
//              navController = navController,
//              onSearchBarValueChange = onSearchBarValueChange,
              searchBarText = searchBarText,
              onClientEdit = onClientEdit,
              onClientRecharge = onClientRecharge,
              onClientSendMessage = onClientSendMessage,
              onClientDelete = { client, onClientDeleteDismissed ->
                coroutineScope.launch {
                  val snackbarResult = snackbarHostState.showSnackbar(
                    message = "${client.name.truncate(20)} eliminado",
                    actionLabel = "Deshacer",
                    duration = SnackbarDuration.Short,
                  )
                  when (snackbarResult) {
                    SnackbarResult.Dismissed -> {
                      onClientDelete(client)
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
              onClientRechargeCounterReset = onClientRechargeCounterReset,
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
      onPickContactButton = {},
      adbInstrumentationRunCommandGetter = { "" },
      runInstrumentationTest = {},
      updateRechargeAvailability = {},
      savePreferencesAction = {},
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
      onClientEdit = {},
      onClientRecharge = {},
      onClientDelete = {},
      onClientSendMessage = { _, _ -> },
      onClientRechargeCounterReset = {},
      onAddClient = {}
    )
  }
}
