package com.conexentools.presentation.components.screens.home.pages.client_list

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.conexentools.core.app.Constants
import com.conexentools.core.util.PreviewComposable
import com.conexentools.core.util.log
import com.conexentools.data.local.model.Client
import com.conexentools.data.repository.AndroidUtilsImpl
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.common.CashTextField
import com.conexentools.presentation.components.common.CreditCardTextField
import com.conexentools.presentation.components.common.CubanPhoneNumberTextField
import com.conexentools.presentation.components.common.LabelSwitch
import com.conexentools.presentation.components.common.MultilineTextField
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import my.nanihadesuka.compose.LazyColumnScrollbar
import kotlin.random.Random

@OptIn(FlowPreview::class)
@Composable
fun ClientsListPage(
  clients: LazyPagingItems<Client>,
  searchBarText: String = "",
  onClientEdit: (Client) -> Unit,
  onClientSendMessage: (String, String?) -> Unit,
  onClientDelete: (Client) -> Unit,
  onClientCardCounterReset: (Client) -> Unit,
  scrollPosition: MutableIntState,
  defaultMobileToSendCashTransferConfirmation: String,
  transferCashInstrumentedTestAdbCommandGetter: (
    recipientCard: String,
    mobileToConfirm: String
  ) -> String,
  onRunTransferCashInstrumentedTest: (
    recipientCard: String,
    mobileToConfirm: String,
    numberToSendWhatsAppMessage: String?
  ) -> Unit,
  recipientReceiveMyMobileNumberAfterCashTransfer: MutableState<Boolean>,
  cashToTransferToClient: MutableState<String>,
  sendWhatsAppMessageOnTransferCashTestCompleted: MutableState<Boolean>,
  whatsAppMessageToSendOnTransferCashTestCompleted: MutableState<String>,
  onTransferCashToClient: (Client, (canExecuteTransferCashInstrumentedTest: Boolean) -> Unit) -> Unit,
  au: AndroidUtils
) {

  val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = scrollPosition.intValue)
  val showTransferCashDialog = remember { mutableStateOf(false) }
  val clientToTransferCash: MutableState<Client?> = remember { mutableStateOf(null) }

  LaunchedEffect(lazyListState) {
    snapshotFlow { lazyListState.firstVisibleItemIndex }
      .debounce(500L)
      .collectLatest { pos ->
        scrollPosition.intValue = pos
      }
  }

//  key(clients) {
  LazyColumnScrollbar(
    listState = lazyListState,
  ) {
    LazyColumn(
      state = lazyListState,
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxSize()
    ) {
      items(
        count = clients.itemCount,
        key = { clients[it]?.hashCode() ?: Random.nextDouble() }
      ) { index ->
        val client = clients[index]
        var isVisibleByFilters by remember { mutableStateOf(true) }
        if (client != null) {
          LaunchedEffect(client, searchBarText) {
            isVisibleByFilters = searchBarText.isBlank() ||
                client.name.contains(searchBarText, ignoreCase = true) ||
                client.cardNumber != null && client.cardNumber!!.contains(
              searchBarText,
              ignoreCase = true
            ) ||
                client.phoneNumber != null && client.phoneNumber!!.contains(
              searchBarText,
              ignoreCase = true
            )
          }

          if (isVisibleByFilters) {
            ClientCard(
              client = client,
              onEdit = onClientEdit,
              onTransferCash = onTransferCashToClient,
              onDelete = { onClientDelete(it) },
              showDivider = index < clients.itemCount - 1,
              onSendMessage = onClientSendMessage,
              onClientCardCounterReset = onClientCardCounterReset,
              clientToTransferCash = clientToTransferCash,
              showTransferCashDialog = showTransferCashDialog,
//              onRunTransferCashInstrumentedTest = onRunTransferCashInstrumentedTest,
//              defaultMobileToSendCashTransferConfirmation = defaultMobileToSendCashTransferConfirmation,
//              recipientReceiveMyMobileNumberAfterCashTransfer = recipientReceiveMyMobileNumberAfterCashTransfer,
//              cashToTransfer = cashToTransfer,
//              sendWhatsAppMessageOnTransferCashTestCompleted = sendWhatsAppMessageOnTransferCashTestCompleted,
//              whatsAppMessageToSendOnTransferCashTestCompleted = whatsAppMessageToSendOnTransferCashTestCompleted,
//              transferCashInstrumentedTestAdbCommandGetter = transferCashInstrumentedTestAdbCommandGetter,
              au = au
            )
          }
        }
      }

      item {
        clients.run {
          when {
            loadState.refresh is LoadState.Loading -> {
              PageLoader(
                modifier = Modifier.height(Constants.Dimens.HorizontalCardHeight)
              )
            }

            loadState.refresh is LoadState.Error -> {
              val error = clients.loadState.refresh as LoadState.Error
              ErrorMessage(
                modifier = Modifier.height(Constants.Dimens.HorizontalCardHeight),
                message = error.error.localizedMessage!!,
                onClickRetry = { retry() }
              )
            }

            loadState.append is LoadState.Loading -> {
              LoadingNextPageItem(modifier = Modifier)
            }

            loadState.append is LoadState.Error -> {
              val error = clients.loadState.append as LoadState.Error
              ErrorMessage(
                modifier = Modifier,
                message = error.error.localizedMessage!!,
                onClickRetry = { retry() }
              )
            }
          }
        }
      }
    }
  }

  // Transfer Cash Dialog
  if (showTransferCashDialog.value) {

    if (clientToTransferCash.value == null)
      throw Exception("client should not be null at this point")

    var mobileToConfirm by remember(clientToTransferCash) {
      mutableStateOf(defaultMobileToSendCashTransferConfirmation.ifEmpty {
        clientToTransferCash.value!!.phoneNumber ?: ""
      })
    }
    var creditCard by remember { mutableStateOf(clientToTransferCash.value!!.cardNumber ?: "") }

    var showAdbCommand by remember { mutableStateOf(false) }

    AlertDialog(
      title = {
        Text(
          text = "Transferir Efectivo",
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Center
        )
      },
      onDismissRequest = {},
      dismissButton = {
        // Cancelar
        if (!showAdbCommand) {
          TextButton(onClick = { showTransferCashDialog.value = false }) {
            Text("Cancelar")
          }
        }
      },
      confirmButton = {
        Row {
          // Atrás
          if (showAdbCommand) {
            TextButton(onClick = { showAdbCommand = false }) {
              Text("Atrás")
            }
          } else {

            // Show ADB command
            TextButton(onClick = { showAdbCommand = true }) {
              Text("Comando ADB")
            }

            // Transfer
            TextButton(onClick = {

              val errorMessage = if (creditCard.length != 16)
                "El número de la tarjeta debe tener 16 dígitos"
              else if (mobileToConfirm.length != 8)
                "El número del móvil a confirmar debe tener 8 dígitos"
              else ""

              if (errorMessage.isEmpty()) {
                showTransferCashDialog.value = false
                onRunTransferCashInstrumentedTest(
                  creditCard,
                  mobileToConfirm,
                  clientToTransferCash.value!!.phoneNumber
                )
              } else {
                au.toast(errorMessage, vibrate = true)
              }
            }) {
              Text("Transferir")
            }
          }
        }
      },
      text = {
        if (showAdbCommand) {
          val command by remember(creditCard, mobileToConfirm) {
            mutableStateOf(
              transferCashInstrumentedTestAdbCommandGetter(
                creditCard,
                mobileToConfirm
              )
            )
          }
          Text(
            text = command,
            modifier = Modifier.clickable {
              au.setClipboard(command)
              au.toast("Comando copiado al portapapeles")
            }
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            item {
              // Credit card
              CreditCardTextField(
                value = creditCard,
                onValueChange = { creditCard = it }
              )

              Spacer(modifier = Modifier.height(Constants.Dimens.Small))

              // Cash
              CashTextField(
                value = cashToTransferToClient.value,
                onValueChange = { cashToTransferToClient.value = it },
                label = "Monto",
                modifier = Modifier.width(160.dp)
              )

              Spacer(modifier = Modifier.height(Constants.Dimens.Small))

              // Mobile to send transfer confirmation message
              CubanPhoneNumberTextField(
                value = mobileToConfirm,
                onValueChange = { mobileToConfirm = it },
                isOutlinedTextField = true,
                label = { Text("Móvil a confirmar") }
              )

              Spacer(modifier = Modifier.height(Constants.Dimens.Small))

              // Recipient receive my phone number
              LabelSwitch(
                label = "Destinatario recibe mi número",
                checked = recipientReceiveMyMobileNumberAfterCashTransfer
              )

              Spacer(modifier = Modifier.height(Constants.Dimens.Small))

              // Send WhatsApp Message On Transfer Cash Test Completed
              LabelSwitch(
                info = "Enviar mensaje por WhatsApp cuando la transferencia se complete satisfactoriamente?",
                label = "Enviar mensaje",
                checked = sendWhatsAppMessageOnTransferCashTestCompleted,
                onCheckedChange = {
                  if (it && clientToTransferCash.value!!.phoneNumber == null) {
                    sendWhatsAppMessageOnTransferCashTestCompleted.value = false
                    au.toast("Este cliente no tiene número asociado")
                  }
                }
              )
              if (sendWhatsAppMessageOnTransferCashTestCompleted.value) {
                MultilineTextField(
                  value = whatsAppMessageToSendOnTransferCashTestCompleted.value,
                  onValueChange = { whatsAppMessageToSendOnTransferCashTestCompleted.value = it },
                  onDeleteText = { whatsAppMessageToSendOnTransferCashTestCompleted.value = "" },
                  label = "Mensaje"
                )
              }
            }
          }
        }
      }
    )
  }
}

@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, apiLevel = 33)
@Composable
private fun PreviewClientsListPage() {
  PreviewComposable {
    ClientsListPage(
      clients = MutableStateFlow(value = PagingData.from(clientsForTesting)).collectAsLazyPagingItems(),
      searchBarText = "",
      onClientEdit = {},
      onClientDelete = { },
//      paddingValues = PaddingValues(all = 0.dp),
      onClientSendMessage = { _, _ -> },
//      onAddClient = {},
      au = AndroidUtilsImpl(LocalContext.current),
      onTransferCashToClient = { _, _ -> },
      scrollPosition = remember { mutableIntStateOf(0) },
//      lazyListState = rememberLazyListState(),
      onClientCardCounterReset = {},
      defaultMobileToSendCashTransferConfirmation = "55123456",
      transferCashInstrumentedTestAdbCommandGetter = { _, _ -> "" },
      onRunTransferCashInstrumentedTest = { _, _, _ -> },
      recipientReceiveMyMobileNumberAfterCashTransfer = remember { mutableStateOf(false) },
      cashToTransferToClient = remember { mutableStateOf("452") },
      sendWhatsAppMessageOnTransferCashTestCompleted = remember { mutableStateOf(true) },
      whatsAppMessageToSendOnTransferCashTestCompleted = remember { mutableStateOf("45254") },
    )
  }
}

val clientsForTesting = listOf(
  Client(
    id = 0,
    name = "Perica sdfsfsdfsdfsdfsdfsdfsdfsdfsdfffffffggggggsdf",
    phoneNumber = "55467811",
    cardNumber = "4567894112341234",
    latestRechargeDateISOString = "2024-04-17T16:10:49.291993300Z",
    imageUriString = null,
    quickMessage = "popop",
    rechargesMade = 45
  ),
  Client(
    id = 1,
    name = "Maria",
    phoneNumber = "55467811",
    cardNumber = "4567-8941-1234-1234",
    latestRechargeDateISOString = "2024-04-17T14:10:49.297831700Z",
    imageUriString = null,
    quickMessage = "erer",
    rechargesMade = 33
  ),
  Client(
    id = 2,
    name = "Luisa",
    phoneNumber = "55467811",
    cardNumber = "4567-8941-1234-1234",
    latestRechargeDateISOString = "2024-03-25T16:53:46.836478800Z",
    imageUriString = null,
    quickMessage = "erer",
    rechargesMade = 45
  )
)