package com.conexentools.presentation.components.screens.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.ContactMail
import androidx.compose.material.icons.rounded.QueuePlayNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.MutableIntState
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.conexentools.R
import com.conexentools.core.app.Constants
import com.conexentools.core.util.PreviewComposable
import com.conexentools.core.util.getActivity
import com.conexentools.core.util.log
import com.conexentools.core.util.navigate
import com.conexentools.core.util.textFilter
import com.conexentools.core.util.truncate
import com.conexentools.data.local.model.Client
import com.conexentools.data.repository.AndroidUtilsImpl
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.common.ScreenSurface
import com.conexentools.presentation.components.common.ScrollableAlertDialog
import com.conexentools.presentation.components.common.SearchAppBar
import com.conexentools.presentation.components.common.enums.ScreenSurfaceContentContainer
import com.conexentools.presentation.components.screens.home.components.HomeScreenFAB
import com.conexentools.presentation.components.screens.home.enums.HomeScreenPage
import com.conexentools.presentation.components.screens.home.pages.client_list.ClientsListPage
import com.conexentools.presentation.components.screens.home.pages.client_list.clientsForTesting
import com.conexentools.presentation.components.screens.home.pages.instrumented_test.InstrumentedTestPage
import com.conexentools.presentation.components.screens.home.state.HomeScreenLoadingState
import com.conexentools.presentation.components.screens.home.state.HomeScreenState
import com.conexentools.presentation.navigation.Screen
import com.conexentools.presentation.theme.LocalTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
  homeScreenState: HomeScreenState,
  navController: NavController,
  page: MutableState<HomeScreenPage?>,
  clientListPageHelpDialogsShowed: MutableState<Boolean>,
  au: AndroidUtils,

  // InstrumentationTest Page
  rechargeMobileInstrumentedTestAdbCommandGetter: () -> String,
  firstClientNumber: MutableState<String>,
  secondClientNumber: MutableState<String?>,
  firstClientRecharge: MutableState<String>,
  secondClientRecharge: MutableState<String>,
  fetchDataFromWA: MutableState<Boolean>,
  pin: MutableState<String>,
  bank: MutableState<String>,
  cardToUseDropDownMenuPosition: MutableState<String>,
  waContactImageUri: MutableState<Uri?>,
  rechargesAvailabilityDateISOString: MutableState<String?>,
  waContact: MutableState<String>,
  onRunInstrumentedTest: () -> Unit,
  whatsAppInstalledVersion: Pair<Long, String>?,
  transfermovilInstalledVersion: Pair<Long, String>?,
  instrumentationAppInstalledVersion: Pair<Long, String>?,

  // ClientList Page
  isManager: MutableState<Boolean>,
  clients: LazyPagingItems<Client>,
  onClientCardEdit: (Client) -> Unit,
  onSubmitClientForDeletion: (Client) -> Unit,
  onDeleteClient: (Client) -> Unit,
  onClientRestoredFromDeletion: (Client) -> Unit,
  onClientCardSendMessage: (String, String?) -> Unit,
  onClientCardCounterReset: (Client) -> Unit,
  onAddClient: () -> Unit,
  onBatchAddClient: () -> Unit,
  clientsListScrollPosition: MutableIntState,
  onTransferCashToClient: (Client, (canExecuteTransferCashInstrumentedTest: Boolean) -> Unit) -> Unit,
  defaultMobileToSendCashTransferConfirmation: String,
  transferCashInstrumentedTestAdbCommandGetter: (recipientCard: String, mobileToConfirm: String) -> String,
  onRunTransferCashInstrumentedTest: (recipientCard: String, mobileToConfirm: String, numberToSendWhatsAppMessage: String?) -> Unit,
  recipientReceiveMyMobileNumberAfterCashTransfer: MutableState<Boolean>,
  cashToTransferToClient: MutableState<String>,
  sendWhatsAppMessageOnTransferCashTestCompleted: MutableState<Boolean>,
  whatsAppMessageToSendOnTransferCashTestCompleted: MutableState<String>
) {

  when (homeScreenState.state) {

    // Loading
    is HomeScreenLoadingState.ScreenLoading -> {
      Row(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        CircularProgressIndicator()
      }
    }

    // Error
    is HomeScreenLoadingState.Error -> {
      Column(
        modifier = Modifier.padding(Constants.Dimens.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Un error inesperado ocurrió ☹️",
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = homeScreenState.state.message ?: "Unknown",
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.error,
        )
      }
    }

    // Success -> Draw HomeScreen
    is HomeScreenLoadingState.Success -> {

      val showRechargeMobileInstrumentedTestAdbCommandDialog = remember { mutableStateOf(false) }

      // ADB Command Dialog
      if (showRechargeMobileInstrumentedTestAdbCommandDialog.value) {
        AlertDialog(
          onDismissRequest = {
            showRechargeMobileInstrumentedTestAdbCommandDialog.value = false
          },
          title = {
            Text("ADB Instrumentation Run Command")
          },
          text = {
            Text(
              text = rechargeMobileInstrumentedTestAdbCommandGetter(),
              modifier = Modifier.clickable {
                au.setClipboard(
                  rechargeMobileInstrumentedTestAdbCommandGetter()
                )
                au.toast("Comando copiado al portapapeles")
              })
          },
          confirmButton = {
            TextButton(
              onClick = {
                showRechargeMobileInstrumentedTestAdbCommandDialog.value = false
              }) {
              Text("Cerrar")
            }
          },
          dismissButton = {
            Row {
              TextButton(
                onClick = {
                  au.openBrowser(Constants.LOCAL_ADB_ARTICLE_URL)
                  showRechargeMobileInstrumentedTestAdbCommandDialog.value = false
                }
              ) {
                Text("ADB local?")
              }

              TextButton(
                onClick = {
                  au.openBrowser(Constants.ADB_DOWNLOAD_PAGE_URL)
                  showRechargeMobileInstrumentedTestAdbCommandDialog.value = false
                }
              ) {
                Text("Descargar ADB")
              }
            }
          },
        )
      }

      val pagerState: PagerState? = page.value?.let {
        rememberPagerState(
          initialPage = it.ordinal,
          pageCount = { HomeScreenPage.entries.count() }
        )
      }

      val coroutineScope = rememberCoroutineScope()
      val snackbarHostState = remember { SnackbarHostState() }
      val isSearchingClients = remember { mutableStateOf(false) }

      val searchBarText = remember { mutableStateOf("") }

      val searchAppBar: @Composable () -> Unit = {
        SearchAppBar(
          text = searchBarText,
          onNavigateBack = { isSearchingClients.value = false },
        )
      }

      ScreenSurface(
        title = stringResource(id = R.string.app_name),
        titleTextAlign = TextAlign.Left,
        customTopAppBar = if (isSearchingClients.value) searchAppBar else null,
        scrollBehavior = null,
        showTopAppBarHorizontalDivider = true,
        onNavigateBack = null,
        padding = PaddingValues(0.dp),
        defaultTopAppBarActions = {
          TopAppBarActions(
            page = page,
            pagerState = pagerState,
            isSearchingClients = isSearchingClients,
            coroutineScope = coroutineScope,
            navController = navController,
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
          if (page.value != null) {
            HomeScreenFAB(
              firstClientNumber = firstClientNumber,
              secondClientNumber = secondClientNumber,
              firstClientRecharge = firstClientRecharge,
              secondClientRecharge = secondClientRecharge,
              fetchDataFromWA = fetchDataFromWA,
              rechargesAvailabilityDateISOString = rechargesAvailabilityDateISOString,
              page = page,
              showAdbRunCommandDialog = showRechargeMobileInstrumentedTestAdbCommandDialog,
              onAddClient = onAddClient,
              onRunInstrumentedTest = onRunInstrumentedTest,
              onBatchAddClient = onBatchAddClient,
            )
          }
        },
        contentContainer = ScreenSurfaceContentContainer.Surface
      ) {

        if (pagerState == null || page.value == null) {
          Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator()
          }
        } else {
          HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.CenterVertically,
          ) { currentPageIndex ->

            var showManagerPasswordDialog by remember { mutableStateOf(false) }

            // Manager Password Dialog
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
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
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
                } else if (page.value!!.isInstrumentedTestPage() && isSearchingClients.value) {
                  isSearchingClients.value = false
                }
              }
            }

            when (HomeScreenPage.fromOrdinal(currentPageIndex)) {
              HomeScreenPage.INSTRUMENTED_TEST -> {
                InstrumentedTestPage(
                  au = au,
                  firstClientNumber = firstClientNumber,
                  secondClientNumber = secondClientNumber,
                  firstClientRecharge = firstClientRecharge,
                  secondClientRecharge = secondClientRecharge,
                  fetchDataFromWA = fetchDataFromWA,
                  pin = pin,
                  bank = bank,
                  cardToUseDropDownMenuPosition = cardToUseDropDownMenuPosition,
                  waContactImageUri = waContactImageUri,
                  rechargesAvailabilityDateISOString = rechargesAvailabilityDateISOString,
                  waContact = waContact,
                  whatsAppInstalledVersion = whatsAppInstalledVersion,
                  transfermovilInstalledVersion = transfermovilInstalledVersion,
                  instrumentationAppInstalledVersion = instrumentationAppInstalledVersion,
                )
              }

              HomeScreenPage.CLIENT_LIST -> {
                if (isManager.value) {

                  var showClientCardHelpDialog by remember { mutableStateOf(!clientListPageHelpDialogsShowed.value) }
                  var showAddClientButtonHelpDialog by remember { mutableStateOf(false) }

                  if (showClientCardHelpDialog) {
                    ScrollableAlertDialog(
                      text = "Deslize la interfaz de un cliente a la izquierda o a la derecha para ejecutar las diferentes acciones:\n\nEnviar Mensaje (WhatsApp)\nTransferir Efectivo\nEditar\nLlamar (CUBACEL)\nEliminar"
                    ) {
                      showClientCardHelpDialog = false
                      showAddClientButtonHelpDialog = true
                    }
                  }

                  if (showAddClientButtonHelpDialog) {
                    ScrollableAlertDialog(
                      text = "Presione el botón 'Añadir Cliente' [➕] para eso mismo y manténgalo presionado para añadir varios clientes a la vez"
                    ) {
                      showAddClientButtonHelpDialog = false
                      clientListPageHelpDialogsShowed.value = true
                    }
                  }

                  ClientsListPage(
                    clients = clients,
                    searchBarText = searchBarText.value,
                    onClientEdit = onClientCardEdit,
                    onTransferCashToClient = onTransferCashToClient,
                    onClientSendMessage = onClientCardSendMessage,
                    onClientDelete = { client ->
                      coroutineScope.launch {
                        onSubmitClientForDeletion(client)
                        val snackbarResult = snackbarHostState.showSnackbar(
                          message = "${client.name.truncate(20)} eliminado",
                          actionLabel = "Deshacer",
                          duration = SnackbarDuration.Short,
                        )
                        when (snackbarResult) {
                          SnackbarResult.Dismissed -> {
                            onDeleteClient(client)
                          }
                          else -> {
                            onClientRestoredFromDeletion(client)
                          }
                        }
                      }
                    },
                    onClientCardCounterReset = onClientCardCounterReset,
                    scrollPosition = clientsListScrollPosition,
                    defaultMobileToSendCashTransferConfirmation = defaultMobileToSendCashTransferConfirmation,
                    transferCashInstrumentedTestAdbCommandGetter = transferCashInstrumentedTestAdbCommandGetter,
                    onRunTransferCashInstrumentedTest = onRunTransferCashInstrumentedTest,
                    recipientReceiveMyMobileNumberAfterCashTransfer = recipientReceiveMyMobileNumberAfterCashTransfer,
                    cashToTransferToClient = cashToTransferToClient,
                    sendWhatsAppMessageOnTransferCashTestCompleted = sendWhatsAppMessageOnTransferCashTestCompleted,
                    whatsAppMessageToSendOnTransferCashTestCompleted = whatsAppMessageToSendOnTransferCashTestCompleted,
                    au = au,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TopAppBarActions(
  page: MutableState<HomeScreenPage?>,
  pagerState: PagerState?,
  isSearchingClients: MutableState<Boolean>,
  coroutineScope: CoroutineScope,
  navController: NavController,
  au: AndroidUtils,
) {

  var dropDownMenuExpanded by remember { mutableStateOf(false) }
  var showRebootAppDialogToLoadDatabaseFromPrimaryExternalStorage by remember { mutableStateOf(false) }
  val writeExternalStoragePermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
      // If write permission is granted restart app to make Dagger Hilt create Database in primary external storage
      if (au.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        showRebootAppDialogToLoadDatabaseFromPrimaryExternalStorage = true
    }
  val actionToGrantReadWritePermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      log("actionToGrantReadWritePermissionLauncher")
      // If write permission is granted restart app to make Dagger Hilt create Database in primary external storage
      if (au.hasExternalStorageReadWriteAccess())
        showRebootAppDialogToLoadDatabaseFromPrimaryExternalStorage = true
    }

  if (showRebootAppDialogToLoadDatabaseFromPrimaryExternalStorage) {
    ScrollableAlertDialog (
      yesNoDialog = true,
      onDismiss = { showRebootAppDialogToLoadDatabaseFromPrimaryExternalStorage = false },
      text = "Reiniciar la aplicación ahora para cargar la base de datos desde la carpeta '${stringResource(R.string.app_name)}' en el almacenamiento interno?",
      onConfirm = { au.restartApp() }
    )
  }

  Row(modifier = Modifier.animateContentSize()) {
    // Page Switcher Button
    IconButton(
      onClick = {
        if (page.value == null || pagerState == null) {
          au.toast("Espere a que la aplicación termine de cargar", vibrate = true)
        } else {
          val targetPage = if (page.value!!.isInstrumentedTestPage()) {
            HomeScreenPage.CLIENT_LIST
          } else {
            if (isSearchingClients.value)
              isSearchingClients.value = false
            HomeScreenPage.INSTRUMENTED_TEST
          }
          coroutineScope.launch {
            pagerState.animateScrollToPage(targetPage.ordinal)
          }
        }
      }) {
      Icon(
        imageVector = if (page.value == null || page.value!!.isInstrumentedTestPage()) Icons.Rounded.ContactMail else Icons.Rounded.QueuePlayNext,
        contentDescription = null,
      )
    }

    // Search Button
    AnimatedVisibility(
      visible = page.value == HomeScreenPage.CLIENT_LIST,
      enter = fadeIn(animationSpec = tween(durationMillis = 200)),
      exit = fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
      IconButton(
        onClick = {
          isSearchingClients.value = true
        }) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = null,
        )
      }
    }
  }

  // More Button
  IconButton(
    onClick = {
      dropDownMenuExpanded = !dropDownMenuExpanded
    }) {

    Icon(
      imageVector = Icons.Default.MoreVert,
      contentDescription = null,
    )

    DropdownMenu(
      expanded = dropDownMenuExpanded,
      onDismissRequest = { dropDownMenuExpanded = false },
      modifier = Modifier.defaultMinSize(minWidth = 80.dp)
    ) {
      DropdownMenuItem(
        text = { Text("Configuración") },
        onClick = {
          dropDownMenuExpanded = false
          navController.navigate(Screen.Settings)
        }
      )
      DropdownMenuItem(
        text = { Text("Ayuda") },
        onClick = {
          dropDownMenuExpanded = false
          navController.navigate(Screen.Help)
        }
      )
      DropdownMenuItem(
        text = { Text("Acerca de") },
        onClick = {
          dropDownMenuExpanded = false
          navController.navigate(Screen.About)
        }
      )
      if (!au.hasExternalStorageReadWriteAccess()) {
        val activityContext = LocalContext.current.getActivity()!!
        DropdownMenuItem(
          text = { Text("Solicitar permiso para escribir en el almacenamiento interno") },
          onClick = {
            dropDownMenuExpanded = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
              val intent = au.openSettings(
                settingsMenuWindow =  Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                flagActivityNewTask = false,
                onlyReturnIntent = true
              )
              actionToGrantReadWritePermissionLauncher.launch(intent)
            } else if (activityContext.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
              writeExternalStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
              // User has selected Deny and don't ask again
              val intent = au.openSettings(
                settingsMenuWindow =  Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                flagActivityNewTask = false,
                onlyReturnIntent = true
              )
              actionToGrantReadWritePermissionLauncher.launch(intent)
            }
          }
        )
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
    HomeScreen(
      homeScreenState = HomeScreenState(HomeScreenLoadingState.Success),
      navController = rememberNavController(),
      page = remember { mutableStateOf(HomeScreenPage.INSTRUMENTED_TEST) },
      clientListPageHelpDialogsShowed = remember { mutableStateOf(true) },
      au = AndroidUtilsImpl(context = context),
      rechargeMobileInstrumentedTestAdbCommandGetter = { "" },
      firstClientNumber = remember { mutableStateOf("55797140") },
      secondClientNumber = remember { mutableStateOf("58469745") },
      firstClientRecharge = remember { mutableStateOf("1234") },
      secondClientRecharge = remember { mutableStateOf("2500") },
      fetchDataFromWA = remember { mutableStateOf(false) },
      pin = remember { mutableStateOf("5555") },
      bank = remember { mutableStateOf("Metropolitano") },
      cardToUseDropDownMenuPosition = remember { mutableStateOf("") },
      waContactImageUri = remember { mutableStateOf(null) },
      rechargesAvailabilityDateISOString = remember { mutableStateOf(null) },
      waContact = remember { mutableStateOf("Jeans MR") },
      onRunInstrumentedTest = {},
      whatsAppInstalledVersion = Pair(23, "123.124.51"),
      transfermovilInstalledVersion = Pair(23, "123.124.51"),
      instrumentationAppInstalledVersion = Pair(23, "123.124.51"),
      isManager = remember { mutableStateOf(false) },
      clients = MutableStateFlow(PagingData.from(clientsForTesting)).collectAsLazyPagingItems(),
      onClientCardEdit = {},
      onTransferCashToClient = { _, _ ->},
      onSubmitClientForDeletion = {},
      onDeleteClient = {},
      onClientRestoredFromDeletion = {},
      onClientCardSendMessage = { _, _ -> },
      onClientCardCounterReset = {},
      onAddClient = {},
      onBatchAddClient = {},
      clientsListScrollPosition = remember { mutableIntStateOf(0) },
      defaultMobileToSendCashTransferConfirmation = "movet",
      transferCashInstrumentedTestAdbCommandGetter = { _, _ -> "" },
      onRunTransferCashInstrumentedTest = { _, _, _ -> },
      recipientReceiveMyMobileNumberAfterCashTransfer = remember { mutableStateOf(true) },
      cashToTransferToClient = remember { mutableStateOf("5555") },
      sendWhatsAppMessageOnTransferCashTestCompleted = remember { mutableStateOf(true) },
      whatsAppMessageToSendOnTransferCashTestCompleted = remember { mutableStateOf("Love at the space station was the definition of shield, deceived to a bare parasite.") },
    )
  }
}
